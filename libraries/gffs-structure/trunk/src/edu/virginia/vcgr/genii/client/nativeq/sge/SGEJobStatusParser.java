package edu.virginia.vcgr.genii.client.nativeq.sge;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;

public class SGEJobStatusParser extends DefaultHandler
{
	static public Map<String, String> parseStatus(String statusDocument) throws NativeQueueException
	{
		try {
			StringReader reader = new StringReader(statusDocument);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(false);
			SGEJobStatusParser parser = new SGEJobStatusParser();
			factory.newSAXParser().parse(new InputSource(reader), parser);
			return parser._statusMap;
		} catch (SAXException se) {
			throw new NativeQueueException("Unable to parse job status document.", se);
		} catch (IOException e) {
			throw new NativeQueueException("Unable to parse job status document.", e);
		} catch (ParserConfigurationException e) {
			throw new NativeQueueException("Unable to parse job status document.", e);
		}
	}

	private Map<String, String> _statusMap = new HashMap<String, String>();

	private String _stateString = null;
	private StringBuilder _tokenBuilder = null;

	private SGEJobStatusParser()
	{
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if (_tokenBuilder != null)
			_tokenBuilder.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if (qName.equals("JB_job_number")) {
			if (_stateString != null && _tokenBuilder != null)
				_statusMap.put(_tokenBuilder.toString().trim(), _stateString);
		}

		_stateString = null;
		_tokenBuilder = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if (qName.equals("job_list"))
			_stateString = attributes.getValue("state");
		else if (qName.equals("JB_job_number")) {
			_tokenBuilder = new StringBuilder();
		} else {
			_tokenBuilder = null;
			_stateString = null;
		}
	}
}