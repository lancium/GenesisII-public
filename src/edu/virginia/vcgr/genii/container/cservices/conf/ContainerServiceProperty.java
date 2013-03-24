package edu.virginia.vcgr.genii.container.cservices.conf;

import javax.xml.bind.annotation.XmlAttribute;

class ContainerServiceProperty
{
	@XmlAttribute(name = "name", required = true)
	private String _name;

	@XmlAttribute(name = "value", required = true)
	private String _value;

	ContainerServiceProperty()
	{
		this(null, null);
	}

	ContainerServiceProperty(String name, String value)
	{
		_name = name;
		_value = value;
	}

	final String name()
	{
		return _name;
	}

	final String value()
	{
		return _value;
	}
}