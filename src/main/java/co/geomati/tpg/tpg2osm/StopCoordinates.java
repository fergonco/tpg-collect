package co.geomati.tpg.tpg2osm;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import co.geomati.tpg.utils.TPG;
import co.geomati.tpg.utils.XPathExaminer;

public class StopCoordinates {

	public static void main(String[] args) throws IOException, SAXException, XPathExpressionException {
		TPG tpg = new TPG();
		String xmlResult = tpg.get("GetStops.xml");
		XPathExaminer examiner = new XPathExaminer(xmlResult);
		NodeList xmlStops = examiner.getAsNodeset("/stops/stops/stop/stopCode");
		StringBuilder stopCodesParameter = new StringBuilder();
		for (int i = 0; i < xmlStops.getLength(); i++) {
			Node xmlStop = xmlStops.item(i);
			stopCodesParameter.append(xmlStop.getTextContent()).append(",");
		}
		stopCodesParameter.setLength(stopCodesParameter.length() - 1);
		xmlResult = tpg.get("GetPhysicalStops.xml", "stopCode=" + stopCodesParameter.toString());
		examiner = new XPathExaminer(xmlResult);
		NodeList xmlPhysicalStops = examiner.getAsNodeset("/stops/stops/stop");
		for (int i = 0; i < xmlPhysicalStops.getLength(); i++) {
			Node xmlPhysicalStop = xmlPhysicalStops.item(i);
			String code = examiner.getAsString(xmlPhysicalStop, "stopCode");
			Node coordinates = examiner.getAsNode(xmlPhysicalStop, "physicalStops/physicalStop/coordinates");
			Double latitude = examiner.getAsDouble(coordinates, "latitude");
			Double longitude = examiner.getAsDouble(coordinates, "longitude");
			String statement = "INSERT INTO tpgstops VALUES ('$code', ST_SetSRID(ST_MakePoint($lon, $lat), 4326));";
			statement = statement.replace("$code", code).replace("$lat", latitude.toString()).replace("$lon",
					longitude.toString());
			System.out.println(statement);
		}
	}
}
