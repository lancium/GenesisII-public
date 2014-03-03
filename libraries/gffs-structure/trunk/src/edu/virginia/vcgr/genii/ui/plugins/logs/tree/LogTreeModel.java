package edu.virginia.vcgr.genii.ui.plugins.logs.tree;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;

public class LogTreeModel extends DefaultTreeModel
{
	static final long serialVersionUID = 0L;

	private UIPluginContext _uiContext;

	LogTreeModel(LogTreeModel original)
	{
		super(new LogTreeNode((LogTreeNode) original.getRoot()));

		_uiContext = original._uiContext;
	}

	public LogTreeModel(UIPluginContext uiContext) throws Throwable
	{
		super(new LogTreeNode(new LogFilledInTreeObject(LogPath.getCurrent())));

		_uiContext = uiContext;
	}

	public UIPluginContext uiPluginContext()
	{
		return _uiContext;
	}

	public ApplicationContext appContext()
	{
		return _uiContext.applicationContext();
	}

	TreePath translatePath(TreePath anotherPath)
	{
		TreePath ret = new TreePath(getRoot());

		for (int lcv = 1; lcv < anotherPath.getPathCount(); lcv++) {
			LogTreeNode parent = (LogTreeNode) ret.getLastPathComponent();
			LogTreeNode next = parent.lookup(anotherPath.getPathComponent(lcv).toString());
			if (next == null)
				return null;

			ret = ret.pathByAddingChild(next);
		}

		return ret;
	}

}