package org.morgan.util.macro;

import java.util.HashMap;
import java.util.Map;

public class PrefixMapMacroResolver implements MacroResolver
{
	private String _defaultPrefix;
	private Map<String, MacroResolver> _prefixMap;
	
	public PrefixMapMacroResolver(String defaultPrefix)
	{
		_defaultPrefix = defaultPrefix;
		_prefixMap = new HashMap<String, MacroResolver>();
	}
	
	public PrefixMapMacroResolver()
	{
		this(null);
	}
	
	final public String defaultPrefix()
	{
		return _defaultPrefix;
	}
	
	final public void defaultPrefix(String defaultPrefix)
	{
		_defaultPrefix = defaultPrefix;
	}
	
	final public void addResolver(String prefix, MacroResolver resolver)
	{
		if (prefix == null || prefix.length() == 0)
			throw new IllegalArgumentException(
				"Prefix cannot be null or empty.");
		
		if (resolver == null)
			throw new IllegalArgumentException(
				"Resolver cannot be null.");
		
		_prefixMap.put(prefix, resolver);
	}
	
	@Override
	final public String lookup(String key)
	{
		String prefix = _defaultPrefix;
		
		int index = key.indexOf(':');
		if (index > 0)
		{
			prefix = key.substring(0, index);
			key = key.substring(index + 1);
		}
		
		if (prefix == null || prefix.length() == 0)
			return null;
		
		MacroResolver resolver = _prefixMap.get(prefix);
		if (resolver == null)
			return null;
		
		return resolver.lookup(key);
	}
}