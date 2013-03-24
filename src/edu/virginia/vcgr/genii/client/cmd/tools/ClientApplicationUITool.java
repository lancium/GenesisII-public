package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.ui.ClientApplication;

public class ClientApplicationUITool extends BaseGridTool
{
	static private final String DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dclient-ui";
	static private final String USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/uclient-ui";
	static private final String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/client-ui";

	static private void setupMacOSProperties()
	{
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Genesis II Application");
		System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
		System.setProperty("com.apple.mrj.application.live-resize", "true");
		System.setProperty("com.apple.macos.smallTabs", "true");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
	}

	private boolean _launchShell = false;

	@Option("shell")
	public void setShell()
	{
		_launchShell = true;
	}

	public ClientApplicationUITool()
	{
		super(new FileResource(DESCRIPTION), new FileResource(USAGE), false, ToolCategory.GENERAL);
		addManPage(new FileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws Throwable
	{
		if (OperatingSystemType.getCurrent() == OperatingSystemType.MACOS)
			setupMacOSProperties();

		ClientApplication ca = new ClientApplication(_launchShell);
		if (!_launchShell) {
			ca.pack();
			GuiUtils.centerComponent(ca);
			ca.setVisible(true);
		} else
			ca.dispose();

		ca.join();

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() > 0)
			throw new InvalidToolUsageException();
	}
}