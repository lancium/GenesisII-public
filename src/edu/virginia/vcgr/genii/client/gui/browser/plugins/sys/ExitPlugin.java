package edu.virginia.vcgr.genii.client.gui.browser.plugins.sys;

import javax.swing.JFrame;

import edu.virginia.vcgr.genii.client.gui.browser.plugins.IMenuPlugin;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginStatus;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class ExitPlugin implements IMenuPlugin
{
	@Override
	public void performAction(RNSPath[] selectedResources, JFrame ownerDialog)
			throws PluginException
	{
		ownerDialog.setVisible(false);
		ownerDialog.dispose();
	}

	@Override
	public PluginStatus getStatus(RNSPath[] selectedResources)
			throws PluginException
	{
		return PluginStatus.ACTIVTE;
	}
}