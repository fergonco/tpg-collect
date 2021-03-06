package co.geomati.tpg;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import co.geomati.tpg.utils.TPG;
import co.geomati.tpg.utils.TPGCachedParser;

public class ThermometerComparator {
	private static final int FORWARD_IGNORING_THERMOMETER_THRESHOLD = 15 * 60 * 1000;

	private final static Logger logger = LogManager.getLogger(ThermometerComparator.class);

	private DayFrame dayFrame;
	private TPGCachedParser tpg;
	private ThermometerArchiver archiver;
	private String line;
	private String firstStop;
	private String destination;
	private ThermometerStart[] thermometerStarts;
	private HashMap<String, Thermometer> departureThermometer;
	private ThermometerListener listener;

	public ThermometerComparator(DayFrame dayFrame, TPGCachedParser tpg, ThermometerArchiver archiver, String line,
			String firstStop, String destination) {
		this(dayFrame, tpg, null, archiver, line, firstStop, destination);
	}

	public ThermometerComparator(DayFrame dayFrame, TPGCachedParser tpg, ThermometerListener listener,
			ThermometerArchiver archiver, String line, String firstStop, String destination) {
		super();
		this.dayFrame = dayFrame;
		this.tpg = tpg;
		this.archiver = archiver;
		this.listener = listener;
		this.line = line;
		this.firstStop = firstStop;
		this.destination = destination;
	}

	public void clear() {
		thermometerStarts = null;
		departureThermometer = null;
	}

	/**
	 * Gets all the thermometers for the first stop of the specified line in the
	 * specified destination
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParseException
	 */
	public void init() throws IOException, SAXException, ParseException {
		if (thermometerStarts == null) {
			logger.debug("Initializing departures for line " + firstStop + "->" + destination);
			thermometerStarts = tpg.getStopDepartures(dayFrame.getCurrentDay(), line, firstStop, destination);
			logger.debug("Number of departures: " + thermometerStarts.length);
		}
		if (departureThermometer == null) {
			departureThermometer = new HashMap<String, Thermometer>();
		}
		for (ThermometerStart start : thermometerStarts) {
			if (!departureThermometer.containsKey(start.getDepartureCode())) {
				try {
					logger.debug("Getting thermometer for departure (" + start.getDepartureCode() + ") in " + firstStop
							+ "->" + destination);
					Thermometer thermometer = tpg.getThermometer(start.getDepartureCode());
					thermometer.setMetadata(line, firstStop, destination);
					if (listener != null) {
						thermometer.setListener(listener);
						thermometer.setDestination(destination);
						thermometer.setLine(line);
					}
					departureThermometer.put(start.getDepartureCode(), thermometer);
					logger.debug("thermometer stored");
				} catch (Exception e) {
					logger.error("Error getting thermometer for departure " + start.getDepartureCode(), e);
				}
			}
		}
	}

	public List<Thermometer> getThermometers() {
		if (thermometerStarts == null) {
			return Collections.<Thermometer>emptyList();
		} else {
			ArrayList<Thermometer> ret = new ArrayList<Thermometer>();
			for (ThermometerStart start : thermometerStarts) {
				ret.add(departureThermometer.get(start.getDepartureCode()));
			}
			return ret;
		}
	}

	public void check() {
		logger.debug("Checking all thermometer starts");
		for (ThermometerStart start : thermometerStarts) {
			logger.debug("Checking " + start.getDepartureCode() + " at " + start.getTimestamp() + " in " + firstStop
					+ "->" + destination);
			long now = new Date().getTime();
			logger.debug("Now: " + now);
			if (start.getTimestamp() - now < FORWARD_IGNORING_THERMOMETER_THRESHOLD) {
				logger.debug("Close enough");
				Thermometer originalThermometer = departureThermometer.get(start.getDepartureCode());
				if (!originalThermometer.isDone()) {
					logger.debug("not yet done. Updating...");
					Thermometer updatedThermometer = null;
					try {
						updatedThermometer = tpg.getThermometer(start.getDepartureCode());
					} catch (IOException e) {
						logger.error("error getting the departures from stop", e);
					} catch (SAXException e) {
						logger.error("Bad XML reading departure plan!", e);
					} catch (ParseException e) {
						logger.error("Cannot parse timestamp", e);
					}

					if (updatedThermometer != null) {
						boolean effectiveUpdate = originalThermometer.update(updatedThermometer);
						logger.debug("updated");
						if (originalThermometer.isComplete()) {
							logger.debug("... and complete. Set to done");
							if (!effectiveUpdate) {
								originalThermometer.setDone(true);
							}
							try {
								archiver.archive(line, firstStop, destination, originalThermometer);
							} catch (IOException e) {
								logger.error("error logging done thermometers", e);
							}
						}
					}
				} else {
					logger.debug("thermometer is done");
				}
			} else {
				logger.debug("Too far");
			}
		}

		report();

	}

	private void report() {
		for (ThermometerStart start : thermometerStarts) {
			long now = new Date().getTime();
			if (start.getTimestamp() - now < FORWARD_IGNORING_THERMOMETER_THRESHOLD) {
				Thermometer thermometer = departureThermometer.get(start.getDepartureCode());
				if (!thermometer.isDone()) {
					thermometer.report();
				}
			}
		}
	}

	public static void main(String[] args) throws IOException, SAXException, ParseException {

		int start4am = 4 * 60 * 60 * 1000;
		int end3amNextDay = 27 * 60 * 60 * 1000;
		DayFrame dayFrame = new DayFrame(start4am, end3amNextDay);

		TPGCachedParser tpg = new TPGCachedParser(new TPG());

		File resultsFolder = new File(System.getenv("CONFIGURATION_FOLDER"));
		ThermometerArchiver archiver = new ThermometerArchiverImpl(resultsFolder);
		WeatherArchiver weatherArchiver = new WeatherArchiverImpl(resultsFolder);
		HumanReadableLog hrLog = new HumanReadableLogImpl(resultsFolder);

		ThermometerComparator comparator1 = new ThermometerComparator(dayFrame, tpg, archiver, "18", "BLAN", "CERN");
		ThermometerComparator comparator2 = new ThermometerComparator(dayFrame, tpg, archiver, "Y", "FEMA",
				"VAL-THOIRY");
		ThermometerMonitor monitor = new ThermometerMonitor(dayFrame,
				new ThermometerComparator[] { comparator1, comparator2 }, weatherArchiver, hrLog);
		monitor.monitor();
	}
}
