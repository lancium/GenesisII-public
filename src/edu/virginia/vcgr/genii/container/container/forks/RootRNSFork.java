package edu.virginia.vcgr.genii.container.container.forks;

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
		addDefaultEntry("Services", ServicesRNSFork.class);
		addDefaultEntry("resources", ResourcesRNSFork.class);
		addDefaultEntry("container.log", ContainerLogFork.class);
	}
}