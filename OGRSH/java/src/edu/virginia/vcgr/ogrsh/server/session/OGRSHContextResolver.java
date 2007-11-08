package edu.virginia.vcgr.ogrsh.server.session;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.IContextResolver;

public class OGRSHContextResolver implements IContextResolver
{
	static private class ContextThreadLocal 
		extends InheritableThreadLocal<ICallingContext>
	{
		protected ICallingContext childValue(ICallingContext parentValue)
		{
			return parentValue.deriveNewContext();
		}
	}
	
	static private ContextThreadLocal _localCallingContext =
		new ContextThreadLocal();
	
	public ICallingContext load() throws IOException, FileNotFoundException
	{
		return _localCallingContext.get().deriveNewContext();
	}

	public void store(ICallingContext ctxt) throws FileNotFoundException,
			IOException
	{
		_localCallingContext.set(ctxt.deriveNewContext());
	}
}