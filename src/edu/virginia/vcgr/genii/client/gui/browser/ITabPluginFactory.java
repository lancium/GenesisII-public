package edu.virginia.vcgr.genii.client.gui.browser;

public interface ITabPluginFactory
{
	public ITabPlugin createTabPlugin()
		throws PluginException;
}
