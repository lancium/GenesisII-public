package edu.virginia.vcgr.genii.client.filesystems.script;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;

class LiteralNumericValueExpression implements NumericValueExpression
{
	private double _value;
	
	public LiteralNumericValueExpression(String literal) 
		throws FilterScriptException
	{
		try
		{
			_value = Double.parseDouble(literal);
		}
		catch (NumberFormatException nfe)
		{
			throw new FilterScriptException(String.format(
				"Literal \"%s\" is not a floating point number.",
				literal), nfe);
		}
	}
	
	@Override
	final public double evaluate(FilesystemUsageInformation usageInformation)
	{
		return _value;
	}
	
	@Override
	final public String toString()
	{
		return Double.toString(_value);
	}
}