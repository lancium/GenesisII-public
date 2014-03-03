package edu.virginia.vcgr.genii.ui.plugins;

import java.util.Properties;

public interface UITopMenuPlugin extends UIMenuPlugin
{
	public void configureTopMenu(Properties properties) throws UIPluginException;

	public void performTopMenuAction(UIPluginContext context) throws UIPluginException;
}