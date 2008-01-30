package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import java.awt.Component;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

/**
 * Tab plugins are plugins which display information and possibly get
 * user input in tabs displayed along-side the browser tree.
 * 
 * @author mmm2a
 */
public interface ITabPlugin extends IPlugin
{
	/**
	 * Retrieve a graphics component that performs the correct functionallity
	 * for the tab.  If it is expensive to create this tab (i.e., it takes a
	 * long time, perhaps because it has to make outcalls to the grid), then
	 * this component should likely be an instance of a class derived off of
	 * the edu.virginia.vcgr.genii.client.gui.browser.grid.ExpensiveInitializationPanel 
	 * class.
	 * 
	 * @param selectedPaths The list of paths selected currently in the browser
	 * tree.
	 * 
	 * @return The new component to display in the tab.
	 * 
	 * @throws PluginException
	 */
	public Component getComponent(
		RNSPath []selectedPaths) throws PluginException;
}