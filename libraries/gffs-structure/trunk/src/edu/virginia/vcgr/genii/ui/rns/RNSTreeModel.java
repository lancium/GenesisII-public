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

	RNSTreeModel(RNSTreeModel original)
	{
		super(new RNSTreeNode((RNSTreeNode) original.getRoot()));

		_uiContext = original._uiContext;
		_appContext = original._appContext;
	}

	public RNSTreeModel(ApplicationContext appContext, UIContext uiContext) throws RNSPathDoesNotExistException
	{
		super(new RNSTreeNode(new RNSFilledInTreeObject(uiContext.callingContext().getCurrentPath().getRoot())), true);

		_uiContext = uiContext;
		_appContext = appContext;
	}

	public UIContext uiContext()
	{
		return _uiContext;
	}

	final ApplicationContext appContext()
	{
		return _appContext;
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