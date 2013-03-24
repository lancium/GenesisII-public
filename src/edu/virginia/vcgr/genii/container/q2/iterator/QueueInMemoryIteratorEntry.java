package edu.virginia.vcgr.genii.container.q2.iterator;

import java.util.Collection;

import edu.virginia.cs.vcgr.genii.job_management.JobInformationType;

public class QueueInMemoryIteratorEntry
{

	private boolean _isIterable;
	private Collection<JobInformationType> _toReturn;
	private Collection<String> _iterableIDs;

	public QueueInMemoryIteratorEntry(boolean isIterable, Collection<JobInformationType> ret, Collection<String> toIterate)
	{
		_isIterable = isIterable;
		_toReturn = ret;
		_iterableIDs = toIterate;
	}

	public boolean isIterable()
	{
		return _isIterable;
	}

	public Collection<JobInformationType> getReturnables()
	{
		return _toReturn;
	}

	public Collection<String> getIterableIDs()
	{
		return _iterableIDs;
	}

}
