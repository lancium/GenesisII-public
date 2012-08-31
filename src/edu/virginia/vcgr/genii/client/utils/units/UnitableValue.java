package edu.virginia.vcgr.genii.client.utils.units;

import java.io.Serializable;

public abstract class UnitableValue<U extends Enum<U>> 
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private double _value;
	private U _units;
	
	protected abstract U defaultUnits();
	protected abstract U parseUnits(String textRepresentation);
	public abstract double as(U targetUnits);
	
	protected UnitableValue(double value, U units)
	{
		_value = value;
		_units = (units == null) ? defaultUnits() : units;
	}
	
	protected UnitableValue(double value)
	{
		this(value, null);
	}
	
	protected UnitableValue(String textRepresentation)
	{
		textRepresentation = textRepresentation.trim();
		int lcv;
		
		for (lcv = textRepresentation.length() - 1; lcv >= 0; lcv--)
		{
			char c = textRepresentation.charAt(lcv);
			if (Character.isDigit(c) || c == '.')
				break;
		}
		
		String numbers = textRepresentation.substring(0, lcv + 1).trim();
		String units = textRepresentation.substring(lcv + 1).trim();
		
		if (numbers.length() == 0)
			throw new IllegalArgumentException(String.format(
				"Can't parse %s into a %s", textRepresentation,
				getClass().getName()));
		
		double value = Double.parseDouble(numbers);
		U u = defaultUnits();
		if (units.length() > 0)
			u = parseUnits(units);
		
		_value = value;
		_units = u;
	}
	
	protected UnitableValue()
	{
		this(0.0);
	}
	
	final public double value()
	{
		return _value;
	}
	
	final public U units()
	{
		return (_units == null) ? defaultUnits() : _units;
	}
	
	@Override
	final public String toString()
	{
		return String.format("%f %s", _value, _units);
	}
	
	final public String toIntegralString()
	{
		return String.format("%d %s", (long)_value, _units);
	}
	
	final public String toString(int floatingPrecision)
	{
		String formatString = String.format("%%.%df %%s",
			floatingPrecision);
		return String.format(formatString, _value, _units);
	}
}