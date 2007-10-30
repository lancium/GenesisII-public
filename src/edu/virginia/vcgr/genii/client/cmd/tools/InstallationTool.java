package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.HashMap;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.install.ContainerInformation;
import edu.virginia.vcgr.genii.client.install.InstallationState;

public class InstallationTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Lists current installation state.";
	static final private String _USAGE =
		"list-installations";
	
	public InstallationTool()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		HashMap<String, ContainerInformation> runningContainers = InstallationState.getRunningContainers();
		for (String deploymentName : runningContainers.keySet())
		{
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