package edu.virginia.vcgr.genii.client.gui.browser.plugins.attrs;

import java.awt.Component;

import edu.virginia.vcgr.genii.client.gui.browser.plugins.ITabPlugin;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginStatus;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class ShowAttributesPlugin implements ITabPlugin
{
	@Override
	public Component getComponent(RNSPath[] selectedPaths)
			throws PluginException
	{
		return new ShowAttrsPanel(selectedPaths[0]);
	}

	@Override
	public PluginStatus getStatus(RNSPath[] selectedResources)
			throws PluginException
	{
		if (selectedResources != null && selectedResources.length > 0 
			&& selectedResources[0].exists())
			return PluginStatus.ACTIVTE;
		return PluginStatus.HIDDEN;
	}
}