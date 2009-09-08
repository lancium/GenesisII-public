package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.ui.ClientApplication;

public class ClientApplicationUITool extends BaseGridTool
{
	static private final String DESCRIPTION = "The large Genesis II Client GUI Application";
	static private final String USAGE = "client-ui";
	
	static private void setupMacOSProperties()
	{
		System.setProperty("com.apple.mrj.application.apple.menu.about.name",
			"Genesis II Application");
		System.setProperty("com.apple.mrj.application.growbox.intrudes",
			"false");
		System.setProperty("com.apple.mrj.application.live-resize", "true");
		System.setProperty("com.apple.macos.smallTabs", "true");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
	}
	
	public ClientApplicationUITool()
	{
		super(DESCRIPTION, USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		if (OperatingSystemType.getCurrent() == OperatingSystemType.MACOS)
			setupMacOSProperties();
		
		ClientApplication ca = new ClientApplication();
		ca.pack();
		GuiUtils.centerComponent(ca);
		ca.setVisible(true);
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