package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public interface IPlugin
{
	public PluginStatus getStatus(
		RNSPath []selectedResources) throws PluginException;
}