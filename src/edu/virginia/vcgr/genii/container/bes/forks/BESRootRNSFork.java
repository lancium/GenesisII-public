package edu.virginia.vcgr.genii.container.bes.forks;

import java.util.Map;

import edu.virginia.vcgr.genii.container.common.forks.CommonRootRNSFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

public class BESRootRNSFork extends CommonRootRNSFork
{
	public BESRootRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	protected void addEntries(Map<String, ResourceForkInformation> entries)
	{
		super.addEntries(entries);
		
		addDefaultEntry("activities", ActivityListRNSResourceFork.class);
	}
}