package edu.virginia.vcgr.genii.container.common.forks;

import java.util.Map;

import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rfork.sfd.StaticRNSResourceFork;

public class CommonRootRNSFork extends StaticRNSResourceFork
{
	static final private String CREATION_PROPERTIES_TITLE = "construction-properties";

	protected CommonRootRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	protected void addEntries(Map<String, ResourceForkInformation> entries)
	{
		addDefaultEntry(CREATION_PROPERTIES_TITLE, ConstructionParametersFork.class);
	}
}
