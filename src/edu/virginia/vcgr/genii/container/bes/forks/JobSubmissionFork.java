package edu.virginia.vcgr.genii.container.bes.forks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

public class JobSubmissionFork 
	extends AbstractStreamableByteIOFactoryResourceFork
{
	public JobSubmissionFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public void modifyState(InputStream source) throws IOException
	{
		// TODO
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public void snapshotState(OutputStream sink) throws IOException
	{
		// do nothing
	}
}