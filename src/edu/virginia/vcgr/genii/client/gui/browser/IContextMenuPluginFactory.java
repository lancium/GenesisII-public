package edu.virginia.vcgr.genii.client.gui.browser;

public interface IContextMenuPluginFactory
{
	public IContextMenuPlugin createContextMenuPlugin()
		throws PluginException;
}