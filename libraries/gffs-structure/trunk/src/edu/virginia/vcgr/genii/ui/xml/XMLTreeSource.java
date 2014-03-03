package edu.virginia.vcgr.genii.ui.xml;

import javax.xml.stream.XMLEventReader;

public interface XMLTreeSource
{
	public XMLEventReader getReader() throws Throwable;
}