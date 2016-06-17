package co.geomati.tpg;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import co.geomati.tpg.utils.TPG;
import co.geomati.tpg.utils.XPathExaminer;

public class ShowLine {
	public static void main(String[] args) throws XPathExpressionException,
			IOException, ParserConfigurationException, SAXException {
		TPG tpg = new TPG();
		String lineCode = "Y";
		String response = tpg.get("GetStops.xml", "lineCode=" + lineCode);
		XPathExaminer examiner = new XPathExaminer(response);
		try {
			NodeList stops = examiner.getAsNodeset("/stops/stops/stop");
			for (int i = 0; i < stops.getLength(); i++) {
				Node stop = stops.item(i);
				String stopCode = examiner.getAsString(stop, "stopCode");
				System.out.println(stopCode);
				NodeList destinations = examiner.getAsNodeset(stop,
						"connections/connection[lineCode='" + lineCode + "']");
				for (int j = 0; j < destinations.getLength(); j++) {
					Node destination = destinations.item(j);
					System.out.println("\t"
							+ examiner.getAsString(destination,
									"destinationCode"));
				}
			}

		} catch (XPathExpressionException e) {
			throw new RuntimeException("My xpath expressions are right!", e);
		}
	}
}
