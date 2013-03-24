package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.cmd.*;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.UserConfig;
import edu.virginia.vcgr.genii.client.configuration.UserConfigUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ContextStreamUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.utils.urls.URLUtilities;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class ConnectTool extends BaseGridTool
{
	static private final String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dconnect";
	static private final String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/uconnect";
	static private final String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/connect";

	public ConnectTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false);
		addManPage(new FileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws Throwable
	{
		GeniiPath gPath = new GeniiPath(getArgument(0));
		String connectURL = gPath.path();
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

	static public void connect(ICallingContext ctxt) throws ResourceException, IOException
	{
		ContextManager.storeCurrentContext(ctxt);
	}

	static public void connect(String connectURL) throws ResourceException, MalformedURLException, IOException
	{
		boolean isWindows = OperatingSystemType.getCurrent().isWindows();

		URL url = URLUtilities.formURL(connectURL, isWindows);
		connect(ContextStreamUtils.load(url), null);
	}

	static public void connect(ICallingContext ctxt, DeploymentName deploymentName) throws ResourceException, IOException
	{
		ContextManager.storeCurrentContext(ctxt);
		if (deploymentName != null) {
			UserConfig userConfig = new UserConfig(deploymentName);
			UserConfigUtils.setCurrentUserConfig(userConfig);

			/*
			 * reload the configuration manager so that all config options are loaded from the
			 * specified deployment dir (instead of likely the "default" deployment).
			 */
			UserConfigUtils.reloadConfiguration();
		}
	}

	static public void connect(String connectURL, DeploymentName deploymentName) throws ResourceException,
		MalformedURLException, IOException
	{
		boolean isWindows = OperatingSystemType.getCurrent().isWindows();

		URL url = URLUtilities.formURL(connectURL, isWindows);
		connect(ContextStreamUtils.load(url), deploymentName);
	}
}