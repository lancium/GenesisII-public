package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class UpdateTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dupdate";
	static final private String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uupdate";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/update";
	
	public UpdateTool()
	{
		super(new FileResource(_DESCRIPTION),new FileResource(_USAGE), false,
				ToolCategory.GENERAL);
		addManPage(new FileResource(_MANPAGE));
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
/*
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
*/
		stdout.println(
	"This functionallity is not supported through the normal command line\n" +
	"client.  Instead, if you want to manually update the grid client, you\n" +
	"need to exit this grid client (and any other grid software that you\n" +
	"might have running on your local machine) and instead issue the\n" +
	"separate \"grid-update\" program.");

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
	}
}
