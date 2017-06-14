package co.geomati.tpg.utils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import co.geomati.tpg.Step;
import co.geomati.tpg.Stop;
import co.geomati.tpg.Thermometer;
import co.geomati.tpg.ThermometerStart;

public class TPGCachedParser {

	private final static Logger logger = LogManager.getLogger(TPGCachedParser.class);

	private TPG tpg;
	private int numAttempts = 1;

	public TPGCachedParser(TPG tpg) {
		this.tpg = tpg;
	}

	public TPGCachedParser(TPG tpg, int numAttempts) {
		this(tpg);
		this.numAttempts = numAttempts;
	}

	private String tpgGet(String command, String... params) throws IOException {
		String xmlContent = null;
		IOException lastException = null;
		for (int i = 0; xmlContent == null && i < numAttempts; i++) {
			if (i > 0) {
				logger.debug(i + "/" + numAttempts + " errors getting " + command + ". Waiting to retry...");
				synchronized (this) {
					try {
						wait(400);
					} catch (InterruptedException e1) {
					}
				}
			}
			try {
				xmlContent = tpg.get(command, params);
			} catch (IOException e) {
				lastException = e;
			}
		}

		if (xmlContent != null) {
			return xmlContent;
		} else {
			throw lastException;
		}
	}

	public Thermometer getThermometer(String departureCode) throws IOException, SAXException, ParseException {
		ArrayList<Step> steps = new ArrayList<Step>();
		String xmlContent = tpgGet("GetThermometer.xml", "departureCode=" + departureCode);
		XPathExaminer examiner = new XPathExaminer(xmlContent);
		try {
			NodeList stepList = examiner.getAsNodeset("/thermometer/steps/step");
			for (int i = 0; i < stepList.getLength(); i++) {
				Node stepNode = stepList.item(i);
				Step step = new Step();
				step.setDepartureCode(examiner.getAsString(stepNode, "departureCode"));
				step.setTimestamp(new TimestampParser().getTime(examiner.getAsString(stepNode, "timestamp")));
				step.setReliable("F".equals(examiner.getAsString(stepNode, "reliability")));
				step.setStopCode(examiner.getAsString(stepNode, "stop/stopCode"));
				steps.add(step);
			}

			return new Thermometer(steps);
		} catch (XPathExpressionException e) {
			throw new RuntimeException("My xpath expressions are right!", e);
		}
	}

	public ThermometerStart[] getStopDepartures(Date today, String lineCode, String stopCode, String destinationCode)
			throws IOException, SAXException, ParseException {
		try {
			NodeList departureList = null;
			XPathExaminer examiner = null;
			int i = 0;
			while (i < numAttempts) {
				String xmlContent = tpgGet("GetAllNextDepartures.xml", "stopCode=" + stopCode, "lineCode=" + lineCode,
						"destinationCode=" + destinationCode);

				examiner = new XPathExaminer(xmlContent);
				departureList = examiner.getAsNodeset("/nextDepartures/departures/departure");
				if (departureList.getLength() > 0) {
					break;
				} else {
					i++;
				}
			}
			ArrayList<ThermometerStart> ret = new ArrayList<ThermometerStart>();
			for (int j = 0; j < departureList.getLength(); j++) {
				Node departure = departureList.item(j);
				ThermometerStart start = new ThermometerStart();
				start = new ThermometerStart();
				start.setDepartureCode(examiner.getAsString(departure, "departureCode"));
				start.setTimestamp(new TimestampParser().getTime(examiner.getAsString(departure, "timestamp")));
				ret.add(start);
			}

			return ret.toArray(new ThermometerStart[ret.size()]);
		} catch (XPathExpressionException e) {
			throw new RuntimeException("All our xpath expressions are right, weird!", e);
		}
	}

	public Stop[] getStops(String lineCode) throws IOException, SAXException, ParseException {
		try {
			NodeList stopCodeList = null;
			XPathExaminer examiner = null;
			int i = 0;
			while (i < numAttempts) {
				String xmlContent = tpgGet("GetStops.xml", "lineCode=" + lineCode);

				examiner = new XPathExaminer(xmlContent);
				stopCodeList = examiner.getAsNodeset("/stops/stops/stop/stopCode");
				if (stopCodeList.getLength() > 0) {
					break;
				} else {
					i++;
				}
			}
			ArrayList<String> stopCodes = new ArrayList<String>();
			for (int j = 0; j < stopCodeList.getLength(); j++) {
				stopCodes.add(stopCodeList.item(j).getTextContent());
			}
			return buildStopList(stopCodes);
		} catch (XPathExpressionException e) {
			throw new RuntimeException("All our xpath expressions are right, weird!", e);
		}
	}

	private Stop[] buildStopList(ArrayList<String> stopCodes) throws IOException, SAXException {
		try {
			NodeList stopList = null;
			XPathExaminer examiner = null;
			int i = 0;
			while (i < numAttempts) {
				String xmlContent = tpgGet("GetPhysicalStops.xml", "stopCode=" + StringUtils.join(stopCodes, ","));

				examiner = new XPathExaminer(xmlContent);
				stopList = examiner.getAsNodeset("/stops/stops/stop");
				if (stopList.getLength() > 0) {
					break;
				} else {
					i++;
				}
			}
			ArrayList<Stop> ret = new ArrayList<Stop>();
			for (int j = 0; j < stopList.getLength(); j++) {
				Node stopNode = stopList.item(j);
				String stopCode = examiner.getAsString(stopNode, "stopCode");
				Node coordinates = examiner.getAsNode(stopNode, "physicalStops/physicalStop/coordinates");
				Double latitude = examiner.getAsDouble(coordinates, "latitude");
				Double longitude = examiner.getAsDouble(coordinates, "longitude");
				Stop stop = new Stop(stopCode, latitude, longitude);
				ret.add(stop);
			}
			return ret.toArray(new Stop[ret.size()]);
		} catch (XPathExpressionException e) {
			throw new RuntimeException("All our xpath expressions are right, weird!", e);
		}
	}
}
