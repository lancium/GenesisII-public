package edu.virginia.vcgr.externalapp;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

class DefaultExternalApplicationConfiguration
{
	@XmlAttribute(name = "name", required = true)
	private String _description = null;

	@XmlElement(name = "argument", required = false)
	private Collection<String> _arguments = new LinkedList<String>();

	final String description()
	{
		return _description;
	}

	final Collection<String> arguments()
	{
		return _arguments;
	}
}
