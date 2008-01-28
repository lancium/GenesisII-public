package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import java.awt.Component;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public interface ITabPlugin extends IPlugin
{
	public Component getComponent(
		RNSPath []selectedPaths) throws PluginException;
}