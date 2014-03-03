package edu.virginia.vcgr.genii.ui.plugins.logs.tree;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTree;
// import javax.swing.event.AncestorEvent;
// import javax.swing.event.AncestorListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
// import edu.virginia.vcgr.genii.ui.rns.DirectoryChangeListener;
import edu.virginia.vcgr.genii.ui.utils.CommonKeyStrokes;

public class LogTree extends JTree
{

	private DisplayByType _displayBy = DisplayByType.DISPLAY_BY_RPC_ID;

	private class RefreshAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private RefreshAction()
		{
			super("Refresh");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			TreePath[] paths = getSelectionPaths();
			if (paths == null)
				return;

			for (TreePath path : getSelectionPaths())
				((LogTreeNode) path.getLastPathComponent()).refresh(LogTree.this, _displayBy);
		}
	}

	private class TreeWillExpandListenerImpl implements TreeWillExpandListener
	{
		@Override
		public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException
		{
			TreePath path = event.getPath();
			LogTreeNode node = (LogTreeNode) path.getLastPathComponent();
			LogTreeObject obj = (LogTreeObject) node.getUserObject();
			if (obj.objectType() == LogTreeObjectType.ENDPOINT_OBJECT)
				_expandedNodes.remove(((LogFilledInTreeObject) obj).path().pwd());
			node.collapse();
		}

		@Override
		public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException
		{
			TreePath path = event.getPath();
			LogTreeNode node = (LogTreeNode) path.getLastPathComponent();
			LogTreeObject obj = (LogTreeObject) node.getUserObject();
			if (obj.objectType() == LogTreeObjectType.ENDPOINT_OBJECT)
				_expandedNodes.put(((LogFilledInTreeObject) obj).path().pwd(), node);
			node.noteExpansion();
			if (node.nodeState() == LogTreeNodeState.NEEDS_EXPANSION)
				node.expand(LogTree.this, _displayBy);
		}
	}

	private static final long serialVersionUID = 1L;
	static final public Dimension DESIRED_BROWSER_SIZE = new Dimension(300, 300);

	private Collection<LogTreeListener> _listeners = new LinkedList<LogTreeListener>();

	private RefreshAction _refreshAction = new RefreshAction();
	private Map<String, LogTreeNode> _expandedNodes = new HashMap<String, LogTreeNode>();

	public LogTree(UIPluginContext uiContext) throws Throwable
	{
		this(new LogTreeModel(uiContext));
	}

	private LogTree(LogTreeModel model)
	{
		super(model);

		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setSelectionModel(selectionModel);

		setCellRenderer(new LogTreeCellRenderer(model.appContext()));
		setAutoscrolls(true);
		setEditable(false);
		setShowsRootHandles(true);
		setExpandedState(new TreePath(((LogTreeNode) model.getRoot()).getPath()), false);

		addTreeWillExpandListener(new TreeWillExpandListenerImpl());

		setupInputMap(getInputMap());
		setupActionMap(getActionMap());

		setDragEnabled(false);

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int selRow = getRowForLocation(e.getX(), e.getY());
				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 1)
						fireLogTreePathClicked(selRow, selPath);
					else if (e.getClickCount() == 2)
						fireLogTreePathDoubleClicked(selRow, selPath);
				}
			}
		});

		// expand the top-level node so that we can see the tree a bit better.
		this.expandRow(0);
	}

	private void fireLogTreePathClicked(int row, TreePath path)
	{
		LogTreeNode node = (LogTreeNode) (path.getLastPathComponent());
		LogTreeObject obj = (LogTreeObject) node.getUserObject();
		if (obj.objectType() == LogTreeObjectType.ENDPOINT_OBJECT) {
			LogFilledInTreeObject fObj = (LogFilledInTreeObject) obj;
			fireLogTreePathClicked(fObj);
		}
	}

	private void fireLogTreePathClicked(LogFilledInTreeObject fObj)
	{
		LogTreeModel model = (LogTreeModel) getModel();

		Collection<LogTreeListener> listeners;

		synchronized (_listeners) {
			listeners = new ArrayList<LogTreeListener>(_listeners);
		}

		for (LogTreeListener listener : listeners)
			listener.pathClicked(this, model.uiPluginContext().uiContext(), fObj);
	}

	private void fireLogTreePathDoubleClicked(int row, TreePath path)
	{
		LogTreeNode node = (LogTreeNode) (path.getLastPathComponent());
		LogTreeObject obj = (LogTreeObject) node.getUserObject();
		if (obj.objectType() == LogTreeObjectType.ENDPOINT_OBJECT) {
			LogFilledInTreeObject fObj = (LogFilledInTreeObject) obj;
			fireLogTreePathDoubleClicked(fObj);
		}
	}

	private void fireLogTreePathDoubleClicked(LogFilledInTreeObject fObj)
	{
		LogTreeModel model = (LogTreeModel) getModel();

		Collection<LogTreeListener> listeners;

		synchronized (_listeners) {
			listeners = new ArrayList<LogTreeListener>(_listeners);
		}

		for (LogTreeListener listener : listeners)
			listener.pathDoubleClicked(this, model.uiPluginContext().uiContext(), fObj);
	}

	public void displayTreeBy(DisplayByType newDisplayBy)
	{
		if (newDisplayBy == null)
			return;

		if (!_displayBy.equals(newDisplayBy)) {
			_displayBy = newDisplayBy;
			// cause a refresh at the root if the displayType is changed

			((LogTreeNode) this.getModel().getRoot()).refresh(LogTree.this, _displayBy);
		}
	}

	public void addLogTreeListener(LogTreeListener listener)
	{
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	public void removeLogTreeListener(LogTreeListener listener)
	{
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}

	private void setupActionMap(ActionMap aMap)
	{
		aMap.put(_refreshAction.getValue(Action.NAME), _refreshAction);
	}

	private void setupInputMap(InputMap iMap)
	{
		iMap.put(CommonKeyStrokes.REFRESH, _refreshAction.getValue(Action.NAME));
	}

	public void setDisplayType(DisplayByType displayBy)
	{
		_displayBy = displayBy;
	}
}
