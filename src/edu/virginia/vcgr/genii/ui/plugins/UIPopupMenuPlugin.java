package edu.virginia.vcgr.genii.ui.plugins;

import java.util.Properties;

public interface UIPopupMenuPlugin extends UIMenuPlugin
{
	public void configurePopupMenu(Properties properties)
		throws UIPluginException;
	
	public void performPopupMenuAction(UIPluginContext context)
		throws UIPluginException;
}
