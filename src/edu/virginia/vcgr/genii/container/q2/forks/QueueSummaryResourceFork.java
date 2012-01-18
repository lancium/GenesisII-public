package edu.virginia.vcgr.genii.container.q2.forks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.security.RWXCategory;

public class QueueSummaryResourceFork extends
		AbstractStreamableByteIOFactoryResourceFork
{
	public QueueSummaryResourceFork(ResourceForkService service,
		String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void destroy() throws ResourceException
	{
		super.destroy();
	}
	
	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void modifyState(InputStream source) throws IOException
	{
		throw new IOException("Not allowed to modify the the queue summary.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public void snapshotState(OutputStream sink) throws IOException
	{
		ResourceKey rKey = getService().getResourceKey();
		PrintStream ps = new PrintStream(sink);
		
		try
		{
			QueueManager qMgr = QueueManager.getManager(rKey.getResourceKey());
			qMgr.summarize(ps);
			ps.flush();
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unable to get queue summary.", sqe);
		}
	}
}