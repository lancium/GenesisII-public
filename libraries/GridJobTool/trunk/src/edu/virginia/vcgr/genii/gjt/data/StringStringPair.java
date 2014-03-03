package edu.virginia.vcgr.genii.gjt.data;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;

public class StringStringPair
{
	@XmlAttribute(name = "name")
	private String _name;

	@XmlAttribute(name = "value")
	private String _value;

	public StringStringPair(String name, String value)
	{
		_name = name;
		_value = value;
	}

	public StringStringPair()
	{
		this(null, null);
	}

	final public String name()
	{
		return _name;
	}

	final public void name(String name, ParameterizableBroker pBroker, ModificationBroker mBroker)
	{
		pBroker.fireParameterizableStringModified(_name, name);
		_name = name;
		mBroker.fireJobDescriptionModified();
	}

	final public String value()
	{
		return _value;
	}

	final public void value(String value, ParameterizableBroker pBroker, ModificationBroker mBroker)
	{
		pBroker.fireParameterizableStringModified(_value, value);
		_value = value;
		mBroker.fireJobDescriptionModified();
	}
}