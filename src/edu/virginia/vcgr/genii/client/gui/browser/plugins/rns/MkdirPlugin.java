package edu.virginia.vcgr.genii.client.gui.browser.plugins.rns;

import javax.swing.JFrame;

import edu.virginia.vcgr.genii.client.gui.browser.grid.IActionContext;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.IMenuPlugin;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginStatus;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class MkdirPlugin implements IMenuPlugin
{
	@Override
	public void performAction(RNSPath[] selectedResources, JFrame ownerDialog,
		IActionContext actionContext)
			throws PluginException
	{
		String name = NewDirectoryDialog.getDirectoryName(ownerDialog);
		
		// TODO
		System.err.println("New Directory is \"" + name + "\".");
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
				if (info.isRNS())
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