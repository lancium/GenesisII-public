package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.HashMap;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.install.ContainerInformation;
import edu.virginia.vcgr.genii.client.install.InstallationState;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;

public class InstallationTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dlist-installations";
	static final private String _USAGE = "config/tooldocs/usage/ulist-installations";
	static final private LoadFileResource _MANPAGE = new LoadFileResource("config/tooldocs/man/list-installations");

	public InstallationTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), true, ToolCategory.INTERNAL);
		addManPage(_MANPAGE);
	}

	@Override
	protected int runCommand() throws Throwable
	{
		HashMap<String, ContainerInformation> runningContainers = InstallationState.getRunningContainers();
		for (String deploymentName : runningContainers.keySet()) {
			stdout.println("Container \"" + deploymentName + "\" is running at "
				+ runningContainers.get(deploymentName).getContainerURL());
		}

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 0)
			throw new InvalidToolUsageException();
	}
}