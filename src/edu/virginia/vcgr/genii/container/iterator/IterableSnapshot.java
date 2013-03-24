package edu.virginia.vcgr.genii.container.iterator;

import java.util.Collection;

import edu.virginia.vcgr.genii.container.rns.InternalEntry;

public class IterableSnapshot
{
	private Collection<InternalEntry> _returns;
	private InMemoryIteratorWrapper _wrapper;

	public IterableSnapshot(Collection<InternalEntry> entries, InMemoryIteratorWrapper imiw)
	{
		_returns = entries;
		_wrapper = imiw;
	}

	public Collection<InternalEntry> getReturns()
	{
		return _returns;
	}

	public InMemoryIteratorWrapper getWrapper()
	{
		return _wrapper;
	}

}
