package edu.virginia.vcgr.genii.client.context;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MemoryBasedContextResolver implements IContextResolver
{
	private ICallingContext _context = null;

	public MemoryBasedContextResolver(ICallingContext context)
	{
		if (context == null)
			throw new IllegalArgumentException("Context cannot be null.");

		_context = context.deriveNewContext();
	}

	@Override
	public ICallingContext load() throws IOException, FileNotFoundException
	{
		return _context.deriveNewContext();
	}

	@Override
	public void store(ICallingContext ctxt) throws FileNotFoundException, IOException
	{
		if (ctxt == null)
			throw new IllegalArgumentException("Context cannot be null.");

		_context = ctxt.deriveNewContext();
	}

	@Override
	public Object clone()
	{
		return new MemoryBasedContextResolver(_context);
	}
}