package edu.virginia.vcgr.genii.client.filesystems.script;

import java.text.ParseException;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;
import edu.virginia.vcgr.genii.client.utils.units.Size;

class LiteralNumericValueExpression implements NumericValueExpression
{
	private Size _value;
	
	public LiteralNumericValueExpression(String literal) 
		throws FilterScriptException
	{
		try
		{
			_value = Size.parse(literal);
		}
		catch (ParseException nfe)
		{
			throw new FilterScriptException(String.format(
				"Literal \"%s\" is not a valid size.",
				literal), nfe);
		}
	}
	
	@Override
	final public double evaluate(FilesystemUsageInformation usageInformation)
	{
		return _value.getBytes();
	}
	
	@Override
	final public String toString()
	{
		return _value.toString();
	}
}