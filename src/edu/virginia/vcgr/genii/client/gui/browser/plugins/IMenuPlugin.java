package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import javax.swing.JDialog;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public interface IMenuPlugin extends IPlugin
{
	public void performAction(
		RNSPath []selectedResources,
		JDialog ownerDialog) throws PluginException;
}