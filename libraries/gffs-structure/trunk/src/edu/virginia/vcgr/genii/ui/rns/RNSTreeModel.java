package edu.virginia.vcgr.genii.ui.rns;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.UIContext;

public class RNSTreeModel extends DefaultTreeModel
{
	static final long serialVersionUID = 0L;

	private ApplicationContext _appContext;
	private UIContext _uiContext;

	public static enum ShowWhichTypes {
		DIRECTORIES_AND_FILES,
		JUST_FILES,
		JUST_DIRECTORIES
	}

	private ShowWhichTypes _whichTypes;

	RNSTreeModel(RNSTreeModel original)
	{
		super(new RNSTreeNode((RNSTreeNode) original.getRoot()));

		_uiContext = original._uiContext;
		_appContext = original._appContext;
		_whichTypes = original._whichTypes;
	}

	// public RNSTreeModel(ApplicationContext appContext, UIContext uiContext, ShowWhichTypes showFiles) throws RNSPathDoesNotExistException
	// {
	// super(new RNSTreeNode(new RNSFilledInTreeObject(uiContext.callingContext().getCurrentPath().getRoot())), true);
	//
	// _uiContext = uiContext;
	// _appContext = appContext;
	// _whichTypes = showFiles;
	// }

	public RNSTreeModel(ApplicationContext appContext, UIContext uiContext, String startPath, ShowWhichTypes showFiles)
		throws RNSPathDoesNotExistException
	{
		// First we need to create an RNSPath from the string.
		super(new RNSTreeNode(
			new RNSFilledInTreeObject(uiContext.callingContext().getCurrentPath().lookup(startPath == null ? "/" : startPath))), true);
		_uiContext = uiContext;
		_appContext = appContext;
		_whichTypes = showFiles;
	}

	public UIContext uiContext()
	{
		return _uiContext;
	}

	final ApplicationContext appContext()
	{
		return _appContext;
	}

	final ShowWhichTypes whichTypes()
	{
		return _whichTypes;
	}

	public RNSTreeNode treeTop()
	{
		return (RNSTreeNode) getRoot();
	}

	TreePath translatePath(TreePath anotherPath)
	{
		TreePath ret = new TreePath(getRoot());

		for (int lcv = 1; lcv < anotherPath.getPathCount(); lcv++) {
			RNSTreeNode parent = (RNSTreeNode) ret.getLastPathComponent();
			RNSTreeNode next = parent.lookup(anotherPath.getPathComponent(lcv).toString());
			if (next == null)
				return null;

			ret = ret.pathByAddingChild(next);
		}

		return ret;
	}
}