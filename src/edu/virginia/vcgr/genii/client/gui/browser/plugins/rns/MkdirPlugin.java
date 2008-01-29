package edu.virginia.vcgr.genii.client.gui.browser.plugins.rns;

import javax.swing.JFrame;

import edu.virginia.vcgr.genii.client.gui.browser.grid.IActionContext;
import edu.virginia.vcgr.genii.client.gui.browser.grid.ILongRunningAction;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.IMenuPlugin;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginStatus;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class MkdirPlugin implements IMenuPlugin
{
	@Override
	public void performAction(RNSPath[] selectedResources, JFrame ownerDialog,
		IActionContext actionContext)
			throws PluginException
	{
		String name = NewDirectoryDialog.getDirectoryName(ownerDialog);
		if (name == null)
			return;
		
		actionContext.performLongRunningAction(new DirectoryMaker(
			selectedResources[0], name));
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
	
	static private class DirectoryMaker implements ILongRunningAction
	{
		private RNSPath _parent;
		private String _newName;
		
		public DirectoryMaker(RNSPath parent, String newName)
		{
			_parent = parent;
			_newName = newName;
		}
		
		@Override
		public void run(IActionContext actionContext) throws Throwable
		{
			try
			{
				System.err.println("Doing lookup.");
				RNSPath newPath = _parent.lookup(
					_newName, RNSPathQueryFlags.MUST_NOT_EXIST);
				System.err.println("Doing mkdir.");
				newPath.mkdir();
				System.err.println("Doing refresh.");
				actionContext.refreshSubTree(_parent);
				System.err.println("Done.");
			}
			catch (RNSPathAlreadyExistsException e)
			{
				actionContext.reportError("Directory already exists.", e);
			}
		}
	}
}