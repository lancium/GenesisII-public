package edu.virginia.vcgr.genii.container.cservices.ver1;

import javax.xml.bind.annotation.XmlAttribute;

class Version1Variable
{
	@XmlAttribute(name = "name", required = true)
	private String _name = null;

	@XmlAttribute(name = "value", required = true)
	private String _value = null;

	final String name()
	{
		return _name;
	}

	final String value()
	{
		return _value;
	}

	@Override
	final public String toString()
	{
		return String.format("Variable(%s, %s)", _name, _value);
	}
}