package edu.virginia.vcgr.genii.client.filesystems.script;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;
import edu.virginia.vcgr.genii.client.utils.units.Size;
import edu.virginia.vcgr.genii.client.utils.units.SizeUnits;

class LiteralNumericValueExpression implements NumericValueExpression
{
	private Size _value;
	
	public LiteralNumericValueExpression(String literal) 
		throws FilterScriptException
	{
		try
		{
			_value = new Size(literal);
		}
		catch (IllegalArgumentException nfe)
		{
			throw new FilterScriptException(String.format(
				"Literal \"%s\" is not a valid size.",
				literal), nfe);
		}
	}
	
	@Override
	final public double evaluate(FilesystemUsageInformation usageInformation)
	{
		return _value.as(SizeUnits.Bytes);
	}
	
	@Override
	final public String toString()
	{
		return _value.toString();
	}
}