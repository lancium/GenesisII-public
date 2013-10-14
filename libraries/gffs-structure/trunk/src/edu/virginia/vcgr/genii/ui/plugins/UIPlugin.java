package edu.virginia.vcgr.genii.ui.plugins;

import java.util.Collection;
import java.util.Properties;

public interface UIPlugin
{
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions);

	public void configurePlugin(Properties properties) throws UIPluginException;
}