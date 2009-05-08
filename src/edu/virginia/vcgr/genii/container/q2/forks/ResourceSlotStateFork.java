package edu.virginia.vcgr.genii.container.q2.forks;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rfork.sd.SimpleStateResourceFork;
import edu.virginia.vcgr.genii.container.rfork.sd.StateDescription;

@StateDescription
public class ResourceSlotStateFork 
	extends SimpleStateResourceFork<Integer>
{
	public ResourceSlotStateFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	@RWXMapping(RWXCategory.READ)
	protected Integer get() throws Throwable
	{
		ResourceKey rKey = getService().getResourceKey();
			
		QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());
		return mgr.getBESConfiguration(getForkName());
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	protected void set(Integer state) throws Throwable
	{
		int slots = state.intValue();
		if (slots < 0)
			throw new IllegalArgumentException(
				"Resources slots can only be set to non-negative integers.");
		ResourceKey rKey = getService().getResourceKey();
		
		QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());
		mgr.configureBES(getForkName(), slots);
	}
}