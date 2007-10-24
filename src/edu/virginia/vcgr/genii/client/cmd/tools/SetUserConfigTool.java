package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.UserConfig;
import edu.virginia.vcgr.genii.client.configuration.UserConfigUtils;

public class SetUserConfigTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Sets up the user's current configuration information (and stores it in user's directory).";
	static private final String _USAGE =
		"set-user-config <user config dir>";
	
	public SetUserConfigTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		String userConfigDir = getArgument(0);
		File testDir = new File(userConfigDir);
		if (!testDir.exists())
			throw new InvalidToolUsageException("Path " + userConfigDir + " does not exist");
		if (!testDir.isDirectory())
			throw new InvalidToolUsageException("Path " + userConfigDir + " is not a directory");
		if (!testDir.canRead())
			throw new InvalidToolUsageException("Path " + userConfigDir + " is not readable");
		UserConfig userConfig = new UserConfig(testDir.getAbsolutePath());
		UserConfigUtils.setCurrentUserConfig(userConfig);
		
		// reload configuration information so that rest of application uses new configuration information 
		// (necessary for example during a grid script).
		// TODO:  make a switch to program...
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