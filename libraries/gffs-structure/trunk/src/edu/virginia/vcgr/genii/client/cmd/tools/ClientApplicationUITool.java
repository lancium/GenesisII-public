package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.appmgr.os.OperatingSystemType.OperatingSystemTypes;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.ui.BasicFrameWindow;
import edu.virginia.vcgr.genii.ui.ClientApplication;
import edu.virginia.vcgr.genii.ui.UIFrame;

public class ClientApplicationUITool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(ClientApplicationUITool.class);

	static private final String DESCRIPTION = "config/tooldocs/description/dclient-ui";
	static private final String USAGE = "config/tooldocs/usage/uclient-ui";
	static private final String _MANPAGE = "config/tooldocs/man/client-ui";

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
		super(new LoadFileResource(DESCRIPTION), new LoadFileResource(USAGE), false, ToolCategory.GENERAL);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		if (OperatingSystemTypes.MACOS.equals(OperatingSystemType.getCurrent()))
			setupMacOSProperties();

		ClientApplication ca = new ClientApplication(_launchShell);
		if (!_launchShell) {
			ca.pack();
			ca.centerWindowAndMarch();
			ca.setVisible(true);
		} else {
			ca.dispose();
		}

		
		//hmmm: abstract this to a method on basic frame window!
		while (true) {
			ArrayList<BasicFrameWindow> frames = UIFrame.getFrameList();
			for (BasicFrameWindow fram : frames) {

				try {
					if (fram instanceof ClientApplication)
						((ClientApplication) fram).pulseActivities();
				} catch (Exception e) {
					_logger.error("failed to pulse activities on UI frame", e);
				}
			}

			if (BasicFrameWindow.activeFrames() <= 0) {
				/*
				 * we have found that it's time to leave since there are no frames left (although we really only think we'll see this as zero
				 * and not negative).
				 */
				break;
			}
			try {
				Thread.sleep(42);
			} catch (InterruptedException e) {
				// ignored.
			}
		}

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() > 0)
			throw new InvalidToolUsageException();
	}
}