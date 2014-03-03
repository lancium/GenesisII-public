package edu.virginia.vcgr.genii.ui.xml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

class XMLEventFilter
{
	private XMLEventReader _reader;
	private XMLEvent _nextEvent = null;

	static private boolean isGoodEvent(XMLEvent event)
	{
		if (event.isCharacters()) {
			String data = event.asCharacters().getData().trim();
			if (data.length() != 0)
				return true;
		} else if (event.isStartElement())
			return true;
		else if (event.isEndElement())
			return true;

		return false;
	}

	XMLEventFilter(XMLEventReader reader) throws XMLStreamException
	{
		_reader = reader;

		while (_reader.hasNext()) {
			_nextEvent = _reader.nextEvent();
			if (isGoodEvent(_nextEvent))
				return;
		}

		_nextEvent = null;
	}

	public XMLEvent next() throws XMLStreamException
	{
		XMLEvent ret = _nextEvent;

		while (_reader.hasNext()) {
			_nextEvent = _reader.nextEvent();
			if (isGoodEvent(_nextEvent))
				return ret;
		}

		_nextEvent = null;
		return ret;
	}

	public XMLEvent peek()
	{
		return _nextEvent;
	}
}