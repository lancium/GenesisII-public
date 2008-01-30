package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

/**
 * This interface is a base interface that ALL plugins indirectly
 * implement.  It contains the common funtionallity of all types of
 * plugins -- namely, the ability to determine the plugin's status
 * based on what is currently selected in the browser tree.
 * 
 * @author mmm2a
 */
public interface IPlugin
{
	/**
	 * Given a selected set of resources, return the plugin's status.
	 * 
	 * @param selectedResources The list of resources currently selected.
	 * This value can be null or empty.
	 * 
	 * @return The status of the plugin for the given, selected resources.
	 * 
	 * @throws PluginException
	 */
	public PluginStatus getStatus(
		RNSPath []selectedResources) throws PluginException;
}