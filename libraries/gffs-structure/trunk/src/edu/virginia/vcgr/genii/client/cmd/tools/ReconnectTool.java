package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.InstallationProperties;
import edu.virginia.vcgr.genii.client.ApplicationBase.GridStates;
import edu.virginia.vcgr.genii.client.cmd.Driver;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.PermissionDeniedException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class ReconnectTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(ReconnectTool.class);

	static private final String _DESCRIPTION = "config/tooldocs/description/dreconnect";
	static private final String _USAGE = "config/tooldocs/usage/ureconnect";
	static private final String _MANPAGE = "config/tooldocs/man/reconnect";

	public ReconnectTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, InvalidToolUsageException, PermissionDeniedException, UserCancelException,
		RNSPathAlreadyExistsException, RNSPathDoesNotExistException, AuthZSecurityException, IOException
	{
		// force a redo of the connection by dropping current one.
		Driver.dropGridConnection(new PrintWriter(System.out, true), new PrintWriter(System.err, true), new InputStreamReader(System.in));

		// now make a new connection.
		GridStates gridOkay = Driver.establishGridConnection(new PrintWriter(System.out, true), new PrintWriter(System.err, true),
			new InputStreamReader(System.in));

		if (gridOkay.equals(GridStates.CONNECTION_ALREADY_GOOD) || gridOkay.equals(GridStates.CONNECTION_GOOD_NOW)) {
			System.out.println("Successfully connected to '" + InstallationProperties.getSimpleGridName() + "' grid.");
		} else {
			String msg = "failed to reconnect to the grid, result=" + gridOkay.toString();
			System.err.println(msg);
			_logger.error(msg);
			return 1;
		}

		// return successfully.
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		// no arguments expected here.
	}
}
