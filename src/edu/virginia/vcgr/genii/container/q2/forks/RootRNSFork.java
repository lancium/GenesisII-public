package edu.virginia.vcgr.genii.container.q2.forks;

import java.util.Map;

import edu.virginia.vcgr.genii.container.common.forks.CommonRootRNSFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

public class RootRNSFork extends CommonRootRNSFork
{
	public RootRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	protected void addEntries(Map<String, ResourceForkInformation> entries)
	{
		super.addEntries(entries);
		
		addDefaultEntry("resources", ResourcesRNSFork.class);
		addDefaultEntry("resource-management", ResourceManagementRNSFork.class);
		addDefaultEntry("jobs", JobsRNSFork.class);
		addDefaultEntry("submission-point", JobSubmissionFork.class);
		addDefaultEntry("summary", QueueSummaryResourceFork.class);
		addDefaultEntry("is-scheduling-jobs", IsSchedulingPropertyFork.class);
	}
}