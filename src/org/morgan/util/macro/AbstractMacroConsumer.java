package org.morgan.util.macro;

abstract class AbstractMacroConsumer implements MacroConsumer, MacroResolver
{
	private MacroResolver _resolver;
	
	protected AbstractMacroConsumer(MacroResolver resolver)
	{
		_resolver = resolver;
	}
	
	protected MacroResolver resolver()
	{
		return _resolver;
	}
	
	@Override
	final public String lookup(String key)
	{
		return _resolver.lookup(key);
	}
}