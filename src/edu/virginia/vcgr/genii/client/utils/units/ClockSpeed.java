package edu.virginia.vcgr.genii.client.utils.units;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A simple class to store a clock speed.  Clock speeds are nothing more then
 * longs indicating the number of cycles in the speed.  What makes
 * this class special is it's ability to parse clock speed descriptions from
 * strings.
 * 
 * @author mmm2a
 */
@XmlJavaTypeAdapter(ClockSpeedXmlAdapter.class)
public class ClockSpeed extends UnitableValue<ClockSpeedUnits>
{
	static final long serialVersionUID = 0L;

	@Override
	protected ClockSpeedUnits defaultUnits()
	{
		return ClockSpeedUnits.Hertz;
	}

	@Override
	protected ClockSpeedUnits parseUnits(String textRepresentation)
	{
		return ClockSpeedUnits.parse(textRepresentation);
	}
	
	@Override
	public double as(ClockSpeedUnits targetUnits)
	{
		return targetUnits.convert(value(), units());
	}

	public ClockSpeed()
	{
		super();
	}

	public ClockSpeed(double value, ClockSpeedUnits units)
	{
		super(value, units);
	}

	public ClockSpeed(double value)
	{
		super(value);
	}

	public ClockSpeed(String textRepresentation)
	{
		super(textRepresentation);
	}
}