package org.morgan.util.macro;

import java.util.Properties;

public class MacroUtils
{
	private MacroResolver _resolver;
	
	public MacroUtils(MacroResolver resolver)
	{
		if (resolver == null)
			throw new IllegalArgumentException(
				"Resolver cannot be null.");
		
		_resolver = resolver;
	}
	
	final public String toString(String source)
	{
		MacroConsumer consumer = new DefaultMacroConsumer(_resolver);
		StringBuilder builder = new StringBuilder();
		
		for (int lcv = 0; lcv < source.length(); lcv++)
		{
			char c = source.charAt(lcv);
			consumer = consumer.consume(builder, c);
		}
		consumer.consume(builder, null);
		
		return builder.toString();
	}
	
	static public String replaceMacros(Properties properties, String source)
	{
		return replace(source, new PropertiesMacroResolver(properties));
	}
	
	static public String replace(String source, MacroResolver resolver)
	{
		return new MacroUtils(resolver).toString(source);
	}
}