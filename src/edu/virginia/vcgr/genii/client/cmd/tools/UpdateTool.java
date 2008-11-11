package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.appwatcher.ApplicationLauncher;
import edu.virginia.vcgr.appwatcher.ApplicationLauncherConsole;
import edu.virginia.vcgr.appwatcher.Version;
import edu.virginia.vcgr.genii.client.cmd.ToolException;

public class UpdateTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Performs an update of this Genesis II installation.";
	static final private String _USAGE =
		"update";
	
	public UpdateTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		ApplicationLauncherConsole console = ApplicationLauncher.getConsole();
		Version oldVersion = console.currentVersion();
		if (!Version.EMPTY_VERSION.equals(oldVersion))
		{
			console.doUpdates();
			if (!oldVersion.equals(console.currentVersion()))
				stdout.println("You need to restart your client in order " +
					"to update it to the newest version.");
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
	}
}