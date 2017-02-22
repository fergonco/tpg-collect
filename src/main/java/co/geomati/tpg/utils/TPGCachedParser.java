package co.geomati.tpg.utils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import co.geomati.tpg.Step;
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
		String xmlContent = tpgGet("GetAllNextDepartures.xml", "stopCode=" + stopCode, "lineCode=" + lineCode,
				"destinationCode=" + destinationCode);

		ArrayList<ThermometerStart> ret = new ArrayList<ThermometerStart>();

		XPathExaminer examiner = new XPathExaminer(xmlContent);
		try {
			NodeList departureList = examiner.getAsNodeset("/nextDepartures/departures/departure");
			for (int i = 0; i < departureList.getLength(); i++) {
				Node departure = departureList.item(i);
				ThermometerStart start = new ThermometerStart();
				start = new ThermometerStart();
				start.setDepartureCode(examiner.getAsString(departure, "departureCode"));
				start.setTimestamp(new TimestampParser().getTime(examiner.getAsString(departure, "timestamp")));
				ret.add(start);
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException("All our xpath expressions are right, weird!", e);
		}
		return ret.toArray(new ThermometerStart[ret.size()]);

	}

}
