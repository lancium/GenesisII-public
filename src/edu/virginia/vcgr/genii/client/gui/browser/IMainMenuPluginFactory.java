package edu.virginia.vcgr.genii.client.gui.browser;

public interface IMainMenuPluginFactory
{
	public IMainMenuPlugin createMainMenuPlugin()
		throws PluginException;
}