package edu.virginia.vcgr.genii.container.q2.forks;

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
		addDefaultEntry("resources", ResourcesRNSFork.class);
		addDefaultEntry("resource-slots", ResourceSlotsRNSFork.class);
		addDefaultEntry("jobs", JobsRNSFork.class);
		addDefaultEntry("submission-point", JobSubmissionFork.class);
		addDefaultEntry("summary", QueueSummaryResourceFork.class);
	}
}