package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import javax.swing.JFrame;

import edu.virginia.vcgr.genii.client.gui.browser.grid.IActionContext;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

/**
 * The IMenuPlugin interface is the interface that all plugins which wish to participate in menus
 * (be they main menus, or context menus) must implement.
 * 
 * @author mmm2a
 */
public interface IMenuPlugin extends IPlugin
{
	/**
	 * Perform the action indicated by this plugin when the user has selected this menu option.
	 * 
	 * @param selectedResources
	 *            The resource on which the action should be performed.
	 * @param ownerDialog
	 *            The frame that owns the plugin for this action (probably the main browser window).
	 * @param actionContext
	 *            An action context that the plugin can use to request context from the main browser
	 *            frame and the RNS tree.
	 * 
	 * @throws PluginException
	 */
	public void performAction(RNSPath[] selectedResources, JFrame ownerDialog, IActionContext actionContext)
		throws PluginException;
}