package edu.virginia.vcgr.genii.client.pwrapper;

import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlAttribute;

public class ElapsedTime
{
	@XmlAttribute(name = "value", required = true)
	private long _value;

	@XmlAttribute(name = "units", required = true)
	private TimeUnit _units;

	private ElapsedTime()
	{
		_value = 0;
		_units = TimeUnit.NANOSECONDS;
	}

	final public long value()
	{
		return _value;
	}

	final public TimeUnit units()
	{
		return _units;
	}

	final public long as(TimeUnit desiredUnits)
	{
		return desiredUnits.convert(_value, _units);
	}
}
