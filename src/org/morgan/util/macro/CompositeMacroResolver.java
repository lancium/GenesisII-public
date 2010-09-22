package org.morgan.util.macro;


public class CompositeMacroResolver implements MacroResolver
{
	private MacroResolver []_resolvers;
	
	public CompositeMacroResolver(MacroResolver...resolvers)
	{
		_resolvers = resolvers;
	}
	
	@Override
	final public String lookup(String key)
	{
		String ret = null;
		
		for (MacroResolver resolver : _resolvers)
		{
			ret = resolver.lookup(key);
			if (ret != null)
				break;
		}
		
		return ret;
	}
}