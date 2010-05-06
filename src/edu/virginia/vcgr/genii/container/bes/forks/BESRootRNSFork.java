package edu.virginia.vcgr.genii.container.bes.forks;

import java.util.Map;

import edu.virginia.vcgr.genii.client.bes.GeniiBESConstants;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rfork.persprop.PersistedPropertyRNSFork;
import edu.virginia.vcgr.genii.container.rfork.sfd.StaticRNSResourceFork;

public class BESRootRNSFork extends StaticRNSResourceFork
{
	public BESRootRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	protected void addEntries(Map<String, ResourceForkInformation> entries)
	{
		addDefaultEntry("activities", ActivityListRNSResourceFork.class);
		addDefaultEntry(GeniiBESConstants.NATIVE_QUEUE_CONF_CATEGORY,
			PersistedPropertyRNSFork.class);
		
		/*
		addDefaultEntry("status", ActivitySummaryResourceFork.class);
		addDefaultEntry(WorkingDirectoryFork.FORK_BASE_PATH_NAME,
			WorkingDirectoryFork.class);
		*/
	}
}