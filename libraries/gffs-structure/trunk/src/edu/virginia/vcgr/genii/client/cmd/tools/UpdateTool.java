package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class UpdateTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dupdate";
	static final private String _USAGE = "config/tooldocs/usage/uupdate";
	static final private String _MANPAGE = "config/tooldocs/man/update";

	public UpdateTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.GENERAL);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException
	{
		/*
		 * ApplicationLauncherConsole console = ApplicationLauncher.getConsole(); Version oldVersion
		 * = console.currentVersion(); if (!Version.EMPTY_VERSION.equals(oldVersion)) {
		 * console.doUpdates(); if (!oldVersion.equals(console.currentVersion()))
		 * stdout.println("You need to restart your client in order " +
		 * "to update it to the newest version."); }
		 * 
		 * return 0;
		 */
		stdout.println("This functionallity is not supported through the normal command line\n"
			+ "client.  Instead, if you want to manually update the grid client, you\n"
			+ "need to exit this grid client (and any other grid software that you\n"
			+ "might have running on your local machine) and instead issue the\n" + "separate \"grid-update\" program.");

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
	}
}
