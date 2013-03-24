package edu.virginia.vcgr.genii.container.bes.activity.forks;

import java.util.Map;

import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rfork.sfd.StaticRNSResourceFork;

public class RootRNSFork extends StaticRNSResourceFork
{
	public RootRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	protected void addEntries(Map<String, ResourceForkInformation> entries)
	{
		addDefaultEntry("status", ActivitySummaryResourceFork.class);
		addDefaultEntry(WorkingDirectoryFork.FORK_BASE_PATH_NAME, WorkingDirectoryFork.class);
	}
}