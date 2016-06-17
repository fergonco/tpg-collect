package co.geomati.tpg.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XPathExaminer {

	private static DocumentBuilder builder;
	private XPath xpath;
	private Document doc;

	static {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO log it
			e.printStackTrace();
		}
	}

	public XPathExaminer(String xmlContent) throws SAXException {
		try {
			doc = builder
					.parse(new ByteArrayInputStream(xmlContent.getBytes()));
		} catch (IOException e) {
			throw new RuntimeException(
					"IOException reading memory bytes, weird!", e);
		}
		XPathFactory xPathfactory = XPathFactory.newInstance();
		xpath = xPathfactory.newXPath();
	}

	public String getAsString(String expression)
			throws XPathExpressionException {
		return xpath.evaluate(expression, doc);
	}

	public Node getAsNode(String expression) throws XPathExpressionException {
		return (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
	}

	public Double getAsDouble(String expression)
			throws XPathExpressionException {
		return (Double) xpath.evaluate(expression, doc, XPathConstants.NUMBER);
	}

	public NodeList getAsNodeset(Node root, String expression)
			throws XPathExpressionException {
		return (NodeList) xpath.evaluate(expression, root,
				XPathConstants.NODESET);
	}

	public NodeList getAsNodeset(String expression)
			throws XPathExpressionException {
		return getAsNodeset(doc, expression);
	}

	public Node getAsNode(Node item, String expression)
			throws XPathExpressionException {
		return (Node) xpath.evaluate(expression, item, XPathConstants.NODE);
	}

	public String getAsString(Node item, String expression)
			throws XPathExpressionException {
		return (String) xpath.evaluate(expression, item, XPathConstants.STRING);
	}

}
