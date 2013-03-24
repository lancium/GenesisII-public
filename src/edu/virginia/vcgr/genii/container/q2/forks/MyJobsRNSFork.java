package edu.virginia.vcgr.genii.container.q2.forks;

import java.util.Map;

import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rfork.sfd.StaticRNSResourceFork;

public class MyJobsRNSFork extends StaticRNSResourceFork
{
	public MyJobsRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	protected void addEntries(Map<String, ResourceForkInformation> entries)
	{
		addDefaultEntry("queued", JobListingRNSFork.class);
		addDefaultEntry("running", JobListingRNSFork.class);
		addDefaultEntry("finished", JobListingRNSFork.class);
		addDefaultEntry("all", JobListingRNSFork.class);
	}
}