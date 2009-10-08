package edu.virginia.vcgr.genii.client.nativeq;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

public class AdditionalArguments
{
	static public Collection<String> parseAdditionalArguments(
		Properties properties, String propertyPattern)
	{
		LinkedList<String> ret = new LinkedList<String>();
		for (int lcv = 0; ; lcv++)
		{
			String property = String.format(propertyPattern, lcv);
			String value = properties.getProperty(property);
			if (value == null)
				break;
			
			ret.add(value);
		}
		
		return ret;
	}
	
	static public AdditionalArguments parseAdditionalArguments(
		Properties properties,
			String qsubPropertyPattern,
			String qstatPropertyPattern,
			String qdelPropertyPattern)
	{
		return new AdditionalArguments(
			parseAdditionalArguments(properties, qsubPropertyPattern),
			parseAdditionalArguments(properties, qstatPropertyPattern),
			parseAdditionalArguments(properties, qdelPropertyPattern));
	}
	
	private Collection<String> _qsubArguments;
	private Collection<String> _qstatArguments;
	private Collection<String> _qdelArguments;
	
	private AdditionalArguments(Collection<String> qsubArguments,
		Collection<String> qstatArguments, Collection<String> qdelArguments)
	{
		_qsubArguments = qsubArguments;
		_qstatArguments = qstatArguments;
		_qdelArguments = qdelArguments;
	}
	
	final public Collection<String> qsubArguments()
	{
		return _qsubArguments;
	}
	
	final public Collection<String> qstatArguments()
	{
		return _qstatArguments;
	}
	
	final public Collection<String> qdelArguments()
	{
		return _qdelArguments;
	}
}