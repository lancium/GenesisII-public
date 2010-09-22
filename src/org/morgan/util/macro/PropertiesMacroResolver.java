package org.morgan.util.macro;

import java.util.Properties;

public class PropertiesMacroResolver implements MacroResolver
{
	private Properties _properties;
	
	public PropertiesMacroResolver(Properties properties)
	{
		if (properties == null)
			throw new IllegalArgumentException(
				"Properties cannot be null.");
		
		_properties = properties;
	}
	
	@Override
	final public String lookup(String key)
	{
		return _properties.getProperty(key);
	}
}