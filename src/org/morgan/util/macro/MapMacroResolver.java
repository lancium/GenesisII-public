package org.morgan.util.macro;

import java.util.Map;

public class MapMacroResolver implements MacroResolver
{
	private Map<String, ?> _map;
	
	public MapMacroResolver(Map<String, ?> map)
	{
		if (map == null)
			throw new IllegalArgumentException(
				"Map cannot be null.");
		
		_map = map;
	}
	
	@Override
	final public String lookup(String key)
	{
		Object obj = _map.get(key);
		if (obj != null)
			return obj.toString();
		
		return null;
	}
}