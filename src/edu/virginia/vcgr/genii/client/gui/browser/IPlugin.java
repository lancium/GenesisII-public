package edu.virginia.vcgr.genii.client.gui.browser;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public interface IPlugin
{
	static final public String EXIT_GROUP_NAME = "Exit";
	
	public String getName();
	
	public PluginStatus getStatus(
		RNSPath []selectedResources);
}