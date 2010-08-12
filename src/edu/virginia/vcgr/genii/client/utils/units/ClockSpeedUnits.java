package edu.virginia.vcgr.genii.client.utils.units;

public enum ClockSpeedUnits
{
	Hertz(1L, "Hz"),
	Kilohertz(1000L, "KHz"),
	Megahertz(1000L * 1000L, "MHz"),
	Gigahertz(1000L * 1000L * 1000L, "GHz"),
	Terahertz(1000L * 1000L * 1000L * 1000L, "THz");
	
	private long _multiplier;
	private String []_alternateNames;
	
	private ClockSpeedUnits(long multiplier, String...alternateNames)
	{
		_multiplier = multiplier;
		_alternateNames = alternateNames;
	}
	
	public double convert(double sourceValue, ClockSpeedUnits sourceUnits)
	{
		return sourceValue * (sourceUnits._multiplier / _multiplier);
	}
	
	public double toHertz(double sourceValue)
	{
		return convert(sourceValue, ClockSpeedUnits.Hertz);
	}
	
	public double toKilohertz(double sourceValue)
	{
		return convert(sourceValue, ClockSpeedUnits.Kilohertz);
	}
	
	public double toMegahertz(double sourceValue)
	{
		return convert(sourceValue, ClockSpeedUnits.Megahertz);
	}
	
	public double toGigahertz(double sourceValue)
	{
		return convert(sourceValue, ClockSpeedUnits.Gigahertz);
	}
	
	public double toTerahertz(double sourceValue)
	{
		return convert(sourceValue, ClockSpeedUnits.Terahertz);
	}
	
	@Override
	public String toString()
	{
		if (_alternateNames != null && _alternateNames.length > 0)
			return _alternateNames[0];
		
		return super.toString();
	}
	
	static public ClockSpeedUnits parse(String text)
	{
		for (ClockSpeedUnits units : ClockSpeedUnits.values())
		{
			if (text.compareToIgnoreCase(units.name()) == 0)
				return units;
			
			for (String name : units._alternateNames)
				if (text.compareToIgnoreCase(name) == 0)
					return units;
		}
		
		throw new IllegalArgumentException(String.format(
			"Can't match %s to a Clock Speed enumeration value.",
			text));
	}
}