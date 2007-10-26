package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.UserConfig;
import edu.virginia.vcgr.genii.client.configuration.UserConfigUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ContextStreamUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class ConnectTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Connects to an existing net.";
	static private final String _USAGE =
		"connect <connect-url> [<user config dir>]";
	
	public ConnectTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		String connectURL = getArgument(0);
		String userConfigDir = null;
		if (numArguments() > 1)
		{
			userConfigDir = getArgument(1);
			File testUserDir = new File(userConfigDir);
			if (!testUserDir.exists())
				throw new ConfigurationException("User configuration directory " + userConfigDir + " does not exist.");
			if (!testUserDir.isDirectory())
				throw new ConfigurationException("User configuration path " + userConfigDir + " is not a directory.");
			if (!testUserDir.canRead())
				throw new ConfigurationException("User configuration directory " + userConfigDir + " is not readable - check permissions.");
		}
//		else
//			userConfigDir = ConfigurationManager.getUserConfigDir();

		connect(connectURL, userConfigDir);
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1 && numArguments() != 2)
			throw new InvalidToolUsageException();
	}
	

	public void connect(ICallingContext ctxt)
		throws ConfigurationException, ResourceException, IOException
	{
		ContextManager.storeCurrentContext(ctxt);
	}

	public void connect(String connectURL)
		throws ResourceException, MalformedURLException, IOException,
			ConfigurationException
	{
		URL url = new URL(connectURL);
		connect(ContextStreamUtils.load(url), null);
	}

	public void connect(ICallingContext ctxt, String userConfigDir)
		throws ConfigurationException, ResourceException, IOException
	{
		ContextManager.storeCurrentContext(ctxt);
		if (userConfigDir != null)
		{
			UserConfig userConfig = new UserConfig(userConfigDir);
			UserConfigUtils.setCurrentUserConfig(userConfig);
		}
	}
	
	public void connect(String connectURL, String userConfigDir)
		throws ResourceException, MalformedURLException, IOException,
			ConfigurationException
	{
		URL url = new URL(connectURL);
		connect(ContextStreamUtils.load(url), userConfigDir);
	}

}