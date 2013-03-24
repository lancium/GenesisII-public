/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package edu.virginia.vcgr.genii.client.ser;

import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.axis.utils.XMLUtils;

public class XmlUtils
{

	private static Log log = LogFactory.getLog(XmlUtils.class.getName());

	private static DocumentBuilderFactory dbf = getDOMFactory();

	private static DocumentBuilderFactory getDOMFactory()
	{
		DocumentBuilderFactory dbf;

		try {
			dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
		} catch (Exception e) {
			log.error("", e);
			dbf = null;
		}

		return (dbf);
	}

	/**
	 * Get an empty new Document
	 * 
	 * @return Document
	 * @throws ParserConfigurationException
	 *             if construction problems occur
	 */
	public static Document newDocument() throws ParserConfigurationException
	{
		synchronized (dbf) {
			return dbf.newDocumentBuilder().newDocument();
		}
	}

	public static Document newDocument(InputSource inp) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilder db;

		synchronized (dbf) {
			db = dbf.newDocumentBuilder();
		}

		db.setErrorHandler(new XMLUtils.ParserErrorHandler());
		return db.parse(inp);
	}

	public static Document newDocument(InputStream inp) throws ParserConfigurationException, SAXException, IOException
	{
		return newDocument(new InputSource(inp));
	}

	public static Document newDocument(String uri) throws ParserConfigurationException, SAXException, IOException
	{
		// call the authenticated version as there might be
		// username/password info embeded in the uri.
		return newDocument(uri, null, null);
	}

	/**
	 * Create a new document from the given URI, use the username and password if the URI requires
	 * authentication.
	 */
	public static Document newDocument(String uri, String username, String password) throws ParserConfigurationException,
		SAXException, IOException
	{
		InputSource ins = getInputSourceFromURI(uri, username, password);

		Document doc = null;

		try {
			doc = newDocument(ins);
		} finally {
			if (ins.getByteStream() != null) {
				ins.getByteStream().close();
			} else if (ins.getCharacterStream() != null) {
				ins.getCharacterStream().close();
			}
		}

		return doc;
	}

	/**
	 * Utility to get the bytes at a protected uri
	 * 
	 * This will retrieve the URL if a username and password are provided. The java.net.URL class
	 * does not do Basic Authentication, so we have to do it manually in this routine.
	 * 
	 * If no username is provided, we create an InputSource from the uri and let the InputSource go
	 * fetch the contents.
	 * 
	 * @param uri
	 *            the resource to get
	 * @param username
	 *            basic auth username
	 * @param password
	 *            basic auth password
	 */
	private static InputSource getInputSourceFromURI(String uri, String username, String password) throws IOException
	{
		URL wsdlurl = null;

		try {
			wsdlurl = new URL(uri);
		} catch (MalformedURLException e) {
			// we can't process it, it might be a 'simple' foo.wsdl
			// let InputSource deal with it
			return new InputSource(uri);
		}

		// if no authentication, just let InputSource deal with it
		if ((username == null) && (wsdlurl.getUserInfo() == null)) {
			return new InputSource(uri);
		}

		// if this is not an HTTP{S} url, let InputSource deal with it
		if (!wsdlurl.getProtocol().startsWith("http")) {
			return new InputSource(uri);
		}

		URLConnection connection = wsdlurl.openConnection();

		// Does this work for https???
		if (!(connection instanceof HttpURLConnection)) {
			// can't do http with this URL, let InputSource deal with it
			return new InputSource(uri);
		}

		HttpURLConnection uconn = (HttpURLConnection) connection;
		String userinfo = wsdlurl.getUserInfo();
		uconn.setRequestMethod("GET");
		uconn.setAllowUserInteraction(false);
		uconn.setDefaultUseCaches(false);
		uconn.setDoInput(true);
		uconn.setDoOutput(false);
		uconn.setInstanceFollowRedirects(true);
		uconn.setUseCaches(false);

		// username/password info in the URL overrides passed in values
		String auth = null;

		if (userinfo != null) {
			auth = userinfo;
		} else if (username != null) {
			auth = (password == null) ? username : (username + ":" + password);
		}

		if (auth != null) {
			uconn.setRequestProperty("Authorization", "Basic " + XMLUtils.base64encode(auth.getBytes(XMLUtils.getEncoding())));
		}

		uconn.connect();

		return new InputSource(uconn.getInputStream());
	}

	public static String toString(Document doc)
	{
		return XMLUtils.DocumentToString(doc);
	}

	public static String toString(Element element)
	{
		return XMLUtils.ElementToString(element);
	}

	/**
	 * 
	 * Utility function for getting the first child element from a element
	 * 
	 * @param element
	 *            The parent element
	 * @return The first child element if one was found, null otherwise
	 */
	public static Element getFirstChildElement(Element element)
	{
		for (Node currentChild = element.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
			if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) currentChild;
			}
		}

		return null;
	}
}
