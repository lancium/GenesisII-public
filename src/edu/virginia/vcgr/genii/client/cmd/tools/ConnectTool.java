package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import edu.virginia.vcgr.genii.client.cmd.*;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.UserConfig;
import edu.virginia.vcgr.genii.client.configuration.UserConfigUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ContextStreamUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.utils.urls.URLUtilities;
import edu.virginia.vcgr.genii.container.sysinfo.SupportedOperatingSystems;

public class ConnectTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Connects to an existing net.";
	static private final String _USAGE =
		"connect <connect-url|connect-path> [<deployment name>]";
	
	public ConnectTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		String connectURL = getArgument(0);
		String deploymentName = null;
		if (numArguments() > 1)
			deploymentName = getArgument(1);

		connect(connectURL, deploymentName == null ? null : new DeploymentName(deploymentName));
		
		throw new ReloadShellException();
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1 && numArguments() != 2)
			throw new InvalidToolUsageException();
	}
	

	static public void connect(ICallingContext ctxt)
		throws ResourceException, IOException
	{
		ContextManager.storeCurrentContext(ctxt);
	}

	static public void connect(String connectURL)
		throws ResourceException, MalformedURLException, IOException
	{
		boolean isWindows = SupportedOperatingSystems.current().equals(
			SupportedOperatingSystems.WINDOWS);
		
		URL url = URLUtilities.formURL(connectURL, isWindows);
		connect(ContextStreamUtils.load(url), null);
	}

	static public void connect(ICallingContext ctxt, DeploymentName deploymentName)
		throws ResourceException, IOException
	{
		ContextManager.storeCurrentContext(ctxt);
		if (deploymentName != null)
		{
			UserConfig userConfig = new UserConfig(deploymentName);
			UserConfigUtils.setCurrentUserConfig(userConfig);
			
			// reload the configuration manager so that all
			// config options are loaded from the specified deployment dir
			// (instead of likely the "default" deployment)
			UserConfigUtils.reloadConfiguration();
/*			
			File sessionDir = ConfigurationManager.getCurrentConfiguration().getUserDirectory();
			boolean clientRole = ConfigurationManager.getCurrentConfiguration().isClientRole();
			ConfigurationManager.reloadConfiguration(sessionDir.getPath());
			if (clientRole) {
				ConfigurationManager.getCurrentConfiguration().setRoleClient();
			} else {
				ConfigurationManager.getCurrentConfiguration().setRoleServer();
			}
*/			
		}
	}
	
	static public void connect(String connectURL, DeploymentName deploymentName)
		throws ResourceException, MalformedURLException, IOException
	{
		boolean isWindows = SupportedOperatingSystems.current().equals(
			SupportedOperatingSystems.WINDOWS);
		
		URL url = URLUtilities.formURL(connectURL, isWindows);
		connect(ContextStreamUtils.load(url), deploymentName);
	}
}