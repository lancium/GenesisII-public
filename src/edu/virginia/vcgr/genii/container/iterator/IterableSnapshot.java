package edu.virginia.vcgr.genii.container.iterator;

import java.util.Collection;

import edu.virginia.vcgr.genii.container.rns.InternalEntry;

public class IterableSnapshot
{
	private Collection<InternalEntry> _returns;
	private InMemoryIteratorWrapper _wrapper;
	private String _dirPath = null;

	public IterableSnapshot(Collection<InternalEntry> entries, InMemoryIteratorWrapper imiw, String dirPath)
	{
		_returns = entries;
		_wrapper = imiw;
		_dirPath = dirPath;
	}

	public Collection<InternalEntry> getReturns()
	{
		return _returns;
	}

	public InMemoryIteratorWrapper getWrapper()
	{
		return _wrapper;
	}

	public String getDirPath()
	{
		return _dirPath;
	}

}
