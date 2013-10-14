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

/**
 * This plugin is a relatively simple menu plugin which makes it easier for users to create new RNS
 * directories.
 * 
 * @author mmm2a
 */
public class MkdirPlugin implements IMenuPlugin
{
	@Override
	public void performAction(RNSPath[] selectedResources, JFrame ownerDialog, IActionContext actionContext)
		throws PluginException
	{
		/*
		 * Prompt the user with an input dialog to get the name of the new directory.
		 */
		String name = NewDirectoryDialog.getDirectoryName(ownerDialog);
		if (name == null)
			return;

		/*
		 * Once we have the new directory's name, we have to create a long-running action to
		 * actually create the new path.
		 */
		actionContext.performLongRunningAction(new DirectoryMaker(selectedResources[0], name));
	}

	@Override
	public PluginStatus getStatus(RNSPath[] selectedResources) throws PluginException
	{
		try {
			if (selectedResources != null && selectedResources.length == 1) {
				TypeInformation info = new TypeInformation(selectedResources[0].getEndpoint());
				if (info.isRNS())
					return PluginStatus.ACTIVTE;
			}
		} catch (RNSPathDoesNotExistException e) {
			throw new PluginException("Unexpected RNSPath referring to non-existant directory \"" + selectedResources[0].pwd()
				+ "\".", e);
		}

		return PluginStatus.INACTIVE;
	}

	/**
	 * The DirectoryMaker is a long running action that takes care of actually calling out to the
	 * grid to make the requested directory.
	 * 
	 * @author mmm2a
	 */
	static private class DirectoryMaker implements ILongRunningAction
	{
		private RNSPath _parent;
		private String _newName;

		/**
		 * Create a new directory maker task with a given RNSPath as a parent, and the given
		 * directory name as the new directory to create.
		 * 
		 * @param parent
		 *            The existing parent directory under which the new directory is to be created.
		 * @param newName
		 *            The name of the new sub-directory.
		 */
		public DirectoryMaker(RNSPath parent, String newName)
		{
			_parent = parent;
			_newName = newName;
		}

		@Override
		public void run(IActionContext actionContext) throws Throwable
		{
			try {
				RNSPath newPath = _parent.lookup(_newName, RNSPathQueryFlags.MUST_NOT_EXIST);
				newPath.mkdir();

				/*
				 * We need to tell the rns tree in the main browser to update since we have changed
				 * its internal structure.
				 */
				actionContext.refreshSubTree(_parent);
			} catch (RNSPathAlreadyExistsException e) {
				/*
				 * We explicitly deal with this exception as we have a little more information about
				 * what went wrong here.
				 */
				actionContext.reportError("Directory already exists.", e);
			}
		}
	}
}