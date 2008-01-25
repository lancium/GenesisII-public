package edu.virginia.vcgr.genii.client.gui.browser;

import javax.swing.JDialog;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public interface IContextMenuPlugin extends IPlugin
{
	public String getMenuLabel();
	
	public String getGroupName();
	
	public void performAction(
		RNSPath []selectedResources,
		JDialog ownerDialog) throws PluginException;
}