package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.UserConfig;
import edu.virginia.vcgr.genii.client.configuration.UserConfigUtils;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class SetUserConfigTool extends BaseGridTool
{
	static private final String _DESCRIPTION = "config/tooldocs/description/dset-user-config";
	static private final String _USAGE = "config/tooldocs/usage/uset-user-config";
	static private final String _MANPAGE = "config/tooldocs/man/set-user-config";

	public SetUserConfigTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.GENERAL);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException
	{
		DeploymentName deploymentName = new DeploymentName(getArgument(0));
		UserConfig userConfig = new UserConfig(deploymentName);
		UserConfigUtils.setCurrentUserConfig(userConfig);

		// reload configuration information so that rest of application uses new configuration
		// information
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