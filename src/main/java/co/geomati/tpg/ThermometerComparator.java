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

	private final static Logger logger = LogManager
			.getLogger(ThermometerComparator.class);

	private DayFrame dayFrame;
	private TPGCachedParser tpg;
	private ThermometerArchiver archiver;
	private String line;
	private String firstStop;
	private String destination;
	private ThermometerStart[] thermometerStarts;
	private HashMap<String, Thermometer> departureThermometer;

	public ThermometerComparator(DayFrame dayFrame, TPGCachedParser tpg,
			ThermometerArchiver archiver, String line, String firstStop,
			String destination) {
		super();
		this.dayFrame = dayFrame;
		this.tpg = tpg;
		this.archiver = archiver;
		this.line = line;
		this.firstStop = firstStop;
		this.destination = destination;
	}

	public void clear() {
		thermometerStarts = null;
		departureThermometer = null;
	}

	public void init() throws IOException, SAXException, ParseException {
		thermometerStarts = tpg.getStopDepartures(dayFrame.getCurrentDay(),
				line, firstStop, destination);
		departureThermometer = new HashMap<String, Thermometer>();
		for (ThermometerStart start : thermometerStarts) {
			Thermometer thermometer = tpg.getThermometer(start
					.getDepartureCode());
			thermometer.setMetadata(line, firstStop, destination);
			departureThermometer.put(start.getDepartureCode(), thermometer);
		}
	}

	public List<Thermometer> getThermometers() {
		if (thermometerStarts == null) {
			return Collections.<Thermometer> emptyList();
		} else {
			ArrayList<Thermometer> ret = new ArrayList<Thermometer>();
			for (ThermometerStart start : thermometerStarts) {
				ret.add(departureThermometer.get(start.getDepartureCode()));
			}
			return ret;
		}
	}

	public void check() {
		for (ThermometerStart start : thermometerStarts) {
			long now = new Date().getTime();
			if (start.getTimestamp() - now < FORWARD_IGNORING_THERMOMETER_THRESHOLD) {
				Thermometer originalThermometer = departureThermometer
						.get(start.getDepartureCode());
				if (!originalThermometer.isDone()) {
					Thermometer updatedThermometer = null;
					try {
						updatedThermometer = tpg.getThermometer(start
								.getDepartureCode());
					} catch (IOException e) {
						logger.error("error getting the departures from stop",
								e);
					} catch (SAXException e) {
						logger.error("Bad XML reading departure plan!", e);
					} catch (ParseException e) {
						logger.error("Cannot parse timestamp", e);
					}

					if (updatedThermometer != null) {
						boolean effectiveUpdate = originalThermometer
								.update(updatedThermometer);
						if (originalThermometer.isComplete()) {
							if (!effectiveUpdate) {
								originalThermometer.setDone(true);
							}
							try {
								archiver.archive(line, firstStop, destination,
										originalThermometer);
							} catch (IOException e) {
								logger.error("error logging done thermometers",
										e);
							}
						}
					}
				}

			}
		}

		report();

	}

	private void report() {
		for (ThermometerStart start : thermometerStarts) {
			long now = new Date().getTime();
			if (start.getTimestamp() - now < FORWARD_IGNORING_THERMOMETER_THRESHOLD) {
				Thermometer thermometer = departureThermometer.get(start
						.getDepartureCode());
				if (!thermometer.isDone()) {
					thermometer.report();
				}
			}
		}
	}

	public static void main(String[] args) throws IOException, SAXException,
			ParseException {

		int start4am = 4 * 60 * 60 * 1000;
		int end3amNextDay = 27 * 60 * 60 * 1000;
		DayFrame dayFrame = new DayFrame(start4am, end3amNextDay);

		TPGCachedParser tpg = new TPGCachedParser(new TPG());

		File resultsFolder = new File(System.getenv("CONFIGURATION_FOLDER"));
		ThermometerArchiver archiver = new ThermometerArchiver(resultsFolder);
		WeatherArchiver weatherArchiver = new WeatherArchiver(resultsFolder);
		HumanReadableLog hrLog = new HumanReadableLog(resultsFolder);

		ThermometerComparator comparator1 = new ThermometerComparator(dayFrame,
				tpg, archiver, "18", "BLAN", "CERN");
		ThermometerComparator comparator2 = new ThermometerComparator(dayFrame,
				tpg, archiver, "Y", "FEMA", "VAL-THOIRY");
		ThermometerMonitor monitor = new ThermometerMonitor(dayFrame,
				new ThermometerComparator[] { comparator1, comparator2 },
				weatherArchiver, hrLog);
		monitor.monitor();

	}
}
