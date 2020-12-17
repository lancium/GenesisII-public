package edu.virginia.vcgr.genii.client.pwrapper;

import java.util.concurrent.TimeUnit;

public class ElapsedTime
{
	//@XmlAttribute(name = "value", required = true)
	private long _value;

	//@XmlAttribute(name = "units", required = true)
	private TimeUnit _units;

	public ElapsedTime(long value)
	{
		_value = value;
		_units = TimeUnit.MICROSECONDS;
	}

	final public long value()
	{
		return _value;
	}
	
	final public void setValue(long val){
		_value=val;
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
