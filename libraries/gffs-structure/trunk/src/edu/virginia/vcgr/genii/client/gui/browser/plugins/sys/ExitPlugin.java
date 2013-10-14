package edu.virginia.vcgr.genii.client.gui.browser.plugins.sys;

import javax.swing.JFrame;

import edu.virginia.vcgr.genii.client.gui.browser.grid.IActionContext;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.IMenuPlugin;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginStatus;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

/**
 * This is the default exit plugin that is displayed in the menus so that the user of the browser
 * can exit the browser.
 * 
 * @author mmm2a
 */
public class ExitPlugin implements IMenuPlugin
{
	@Override
	public void performAction(RNSPath[] selectedResources, JFrame ownerDialog, IActionContext actionContext)
		throws PluginException
	{
		ownerDialog.setVisible(false);
		ownerDialog.dispose();
	}

	@Override
	public PluginStatus getStatus(RNSPath[] selectedResources) throws PluginException
	{
		return PluginStatus.ACTIVTE;
	}
}