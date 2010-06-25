package edu.virginia.vcgr.genii.client.resource;

import edu.virginia.vcgr.genii.common.XMLCommandParameter;

public class JavaCommandParameter
{
	private String _parameterTypeString;
	private String _parameterName;
	private String _parameterDescription;
	
	JavaCommandParameter(XMLCommandParameter p)
	{
		_parameterTypeString = p.getType();
		_parameterName = p.getName();
		_parameterDescription = p.getDescription();
	}
	
	final public String typeString()
	{
		return _parameterTypeString;
	}
	
	final public String name()
	{
		return _parameterName;
	}
	
	final public String description()
	{
		return _parameterDescription;
	}
	
	@Override
	public String toString()
	{
		if (_parameterName != null)
			return _parameterTypeString + " " + _parameterName;
		
		return _parameterTypeString;
	}
}