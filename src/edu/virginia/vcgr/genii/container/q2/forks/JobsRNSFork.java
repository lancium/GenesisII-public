package edu.virginia.vcgr.genii.container.q2.forks;

import java.util.Map;

import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rfork.sfd.StaticRNSResourceFork;

public class JobsRNSFork extends StaticRNSResourceFork
{
	public JobsRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	protected void addEntries(Map<String, ResourceForkInformation> entries)
	{
		addDefaultEntry("mine", MyJobsRNSFork.class);
		addDefaultEntry("all", JobListingRNSFork.class);
	}
}