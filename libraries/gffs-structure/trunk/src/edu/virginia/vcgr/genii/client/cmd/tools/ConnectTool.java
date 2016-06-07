package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.UserConfig;
import edu.virginia.vcgr.genii.client.configuration.UserConfigUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ContextStreamUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.PermissionDeniedException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.utils.urls.URLUtilities;

public class ConnectTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(ConnectTool.class);

	static private final String _DESCRIPTION = "config/tooldocs/description/dconnect";
	static private final String _USAGE = "config/tooldocs/usage/uconnect";
	static private final String _MANPAGE = "config/tooldocs/man/connect";

	public ConnectTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, InvalidToolUsageException, PermissionDeniedException, UserCancelException,
		RNSPathAlreadyExistsException, RNSPathDoesNotExistException, AuthZSecurityException, IOException
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
		boolean isWindows = OperatingSystemType.isWindows();

		URL url = URLUtilities.formURL(connectURL, isWindows);
		connect(ContextStreamUtils.load(url), null);
	}

	static public void connect(ICallingContext ctxt, DeploymentName deploymentName) throws ResourceException, IOException
	{
		ContextManager.storeCurrentContext(ctxt);
		if (deploymentName != null) {
			System.setProperty(DeploymentName.DEPLOYMENT_NAME_PROPERTY, deploymentName.toString());

			if (_logger.isDebugEnabled())
				_logger.debug("prior to connecting, setting deployment name to '" + deploymentName + "'");

			UserConfig userConfig = new UserConfig(deploymentName);
			UserConfigUtils.setCurrentUserConfig(userConfig);

			/*
			 * reload the configuration manager so that all config options are loaded from the specified deployment dir (instead of likely the
			 * "default" deployment).
			 */
			UserConfigUtils.reloadConfiguration();

			ctxt = ContextManager.getCurrentContext();

			// // ignore first call to let it get warmed up again.
			// ContextManager.isGood(ctxt);
			// // now it should be happy with the context.
			// boolean okay = ContextManager.isGood(ctxt);
			// if (!okay) {
			// _logger.error("reporting context is BAD after reload.");
			// }else {
			// _logger.debug("reporting context is GOOD after reload.");
			// }
		}
	}

	static public void connect(String connectURL, DeploymentName deploymentName) throws ResourceException, MalformedURLException, IOException
	{
		boolean isWindows = OperatingSystemType.isWindows();

		URL url = URLUtilities.formURL(connectURL, isWindows);
		connect(ContextStreamUtils.load(url), deploymentName);
	}
}