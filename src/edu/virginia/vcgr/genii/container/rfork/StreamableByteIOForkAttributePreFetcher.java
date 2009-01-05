package edu.virginia.vcgr.genii.container.rfork;

import java.util.Calendar;

import edu.virginia.vcgr.genii.container.byteio.StreamableByteIOAttributePreFetcher;
import edu.virginia.vcgr.genii.container.resource.IResource;

public class StreamableByteIOForkAttributePreFetcher
	extends StreamableByteIOAttributePreFetcher<IResource>
{
	private StreamableByteIOResourceFork _fork;
	
	public StreamableByteIOForkAttributePreFetcher(
		IResource resource, StreamableByteIOResourceFork fork)
	{
		super(resource);
		
		_fork = fork;
	}
	
	@Override
	protected Calendar getAccessTime() throws Throwable
	{
		return _fork.accessTime();
	}

	@Override
	protected Calendar getCreateTime() throws Throwable
	{
		return _fork.createTime();
	}

	@Override
	protected Calendar getModificationTime() throws Throwable
	{
		return _fork.modificationTime();
	}

	@Override
	protected Long getSize() throws Throwable
	{
		return _fork.size();
	}
}