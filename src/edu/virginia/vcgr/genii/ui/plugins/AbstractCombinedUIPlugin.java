package edu.virginia.vcgr.genii.ui.plugins;

import java.util.Properties;

public abstract class AbstractCombinedUIPlugin
	extends AbstractCombinedUIMenusPlugin
	implements UITabPlugin
{
	@Override
	public void configureTabPlugin(Properties properties)
			throws UIPluginException
	{
		// Ignore
	}
}