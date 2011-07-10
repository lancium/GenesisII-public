package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.UserConfig;
import edu.virginia.vcgr.genii.client.configuration.UserConfigUtils;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class SetUserConfigTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dset-user-config";
	static private final String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uset-user-config";
	static private final String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/set-user-config";
	
	public SetUserConfigTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), 
				false, ToolCategory.GENERAL);
		addManPage(new FileResource(_MANPAGE));
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		DeploymentName deploymentName = new DeploymentName(getArgument(0));
		UserConfig userConfig = new UserConfig(deploymentName);
		UserConfigUtils.setCurrentUserConfig(userConfig);
		
		// reload configuration information so that rest of application uses new configuration information 
		// (necessary for example during a grid script).
		UserConfigUtils.reloadConfiguration();
		return 0;
	}
	
	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if (numArgs != 1)
			throw new InvalidToolUsageException("Missing argument <user config dir>");
		String userConfigDir = getArgument(0);
		if (userConfigDir == null || userConfigDir.length() == 0)
			throw new InvalidToolUsageException("Invalid format for argument <user config dir>");
	}
	

}