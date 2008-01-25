package edu.virginia.vcgr.genii.client.gui.browser;

import java.awt.Component;

public interface ITabPlugin extends IPlugin
{
	public String getTabLabel();
	
	public int getPriority();
	
	public Component getComponent() throws PluginException;
}