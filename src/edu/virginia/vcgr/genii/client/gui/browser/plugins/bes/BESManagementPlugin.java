package edu.virginia.vcgr.genii.client.gui.browser.plugins.bes;

import java.awt.Component;

import edu.virginia.vcgr.genii.client.cmd.tools.besmgr.BESManagerPanel;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.ITabPlugin;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginStatus;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class BESManagementPlugin implements ITabPlugin
{
	@Override
	public Component getComponent(RNSPath[] selectedPaths)
			throws PluginException
	{
		try
		{
			return new BESManagerPanel(ContextManager.getCurrentContext(),
				selectedPaths[0].getEndpoint());
		}
		catch (Throwable cause)
		{
			throw new PluginException(
				"Unable to communicate with BES container.", cause);
		}
	}

	@Override
	public PluginStatus getStatus(RNSPath[] selectedResources)
			throws PluginException
	{
		try
		{
			if (selectedResources != null && selectedResources.length == 1)
			{
				TypeInformation info = new TypeInformation(
					selectedResources[0].getEndpoint());
				if (info.isBESContainer())
					return PluginStatus.ACTIVTE;
			}
		}
		catch (RNSPathDoesNotExistException e)
		{
			throw new PluginException(
				"Unexpected RNSPath referring to non-existant directory \"" 
				+ selectedResources[0].pwd() + "\".", e);
		}
		
		return PluginStatus.INACTIVE;
	}
}