package edu.virginia.vcgr.genii.ui.plugins;

import java.util.Properties;

import javax.swing.JComponent;

public interface UITabPlugin extends UIPlugin
{
	public void configureTabPlugin(Properties properties) throws UIPluginException;

	public JComponent getComponent(UIPluginContext context);
}