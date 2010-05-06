package edu.virginia.vcgr.genii.container.q2.forks;

import java.io.IOException;
import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rfork.sd.SimpleStateResourceFork;
import edu.virginia.vcgr.genii.container.rfork.sd.StateDescription;
import edu.virginia.vcgr.genii.container.rfork.sd.TextStateTranslator;

@StateDescription( { TextStateTranslator.class })
public class IsSchedulingPropertyFork extends SimpleStateResourceFork<Boolean>
{
	@Override
	@RWXMapping(RWXCategory.READ)
	protected Boolean get() throws Throwable
	{
		ResourceKey rKey = getService().getResourceKey();
		
		try
		{
			QueueManager qMgr = QueueManager.getManager(rKey.getResourceKey());
			return qMgr.getScheduler().isSchedulingJobs();
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unable to get queue summary.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	protected void set(Boolean state) throws Throwable
	{
		ResourceKey rKey = getService().getResourceKey();
		
		try
		{
			QueueManager qMgr = QueueManager.getManager(rKey.getResourceKey());
			qMgr.getScheduler().storeIsScheduling(state.booleanValue());
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unable to get queue summary.", sqe);
		}
	}
	
	public IsSchedulingPropertyFork(ResourceForkService service,
			String forkPath)
	{
		super(service, forkPath);
	}
}