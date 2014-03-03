package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import edu.virginia.vcgr.genii.client.history.HistoryEvent;

public class HistoryEventTreeModel implements TreeModel
{
	private Collection<TreeModelListener> _listeners = new LinkedList<TreeModelListener>();

	private HistoryEventTreeNode _originalRoot;
	private HistoryEventTreeNode _currentRoot;

	final protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children)
	{
		TreeModelEvent e = null;
		TreeModelListener[] listeners = null;

		synchronized (_listeners) {
			listeners = _listeners.toArray(new TreeModelListener[_listeners.size()]);
		}

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 1; i >= 0; i -= 1) {
			if (e == null)
				e = new TreeModelEvent(source, path, childIndices, children);
			listeners[i].treeNodesChanged(e);
		}
	}

	final protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children)
	{
		TreeModelEvent e = null;
		TreeModelListener[] listeners = null;

		synchronized (_listeners) {
			listeners = _listeners.toArray(new TreeModelListener[_listeners.size()]);
		}

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 1; i >= 0; i -= 1) {
			if (e == null)
				e = new TreeModelEvent(source, path, childIndices, children);
			listeners[i].treeNodesInserted(e);
		}
	}

	final protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children)
	{
		TreeModelEvent e = null;
		TreeModelListener[] listeners = null;

		synchronized (_listeners) {
			listeners = _listeners.toArray(new TreeModelListener[_listeners.size()]);
		}

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 1; i >= 0; i -= 1) {
			if (e == null)
				e = new TreeModelEvent(source, path, childIndices, children);
			listeners[i].treeNodesRemoved(e);
		}
	}

	final protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children)
	{
		TreeModelEvent e = null;
		TreeModelListener[] listeners = null;

		synchronized (_listeners) {
			listeners = _listeners.toArray(new TreeModelListener[_listeners.size()]);
		}

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 1; i >= 0; i -= 1) {
			if (e == null)
				e = new TreeModelEvent(source, path, childIndices, children);
			listeners[i].treeStructureChanged(e);
		}
	}

	HistoryEventTreeModel(Collection<HistoryEvent> events, HistoryEventFilter initialFilter)
	{
		_originalRoot = HistoryEventTreeNode.formTree(events);
		_currentRoot = HistoryEventTreeNode.formTree(_originalRoot, initialFilter);
		initialFilter.addFilterListener(new HistoryEventFilterListenerImpl());
	}

	@Override
	final public void addTreeModelListener(TreeModelListener l)
	{
		synchronized (_listeners) {
			_listeners.add(l);
		}
	}

	@Override
	final public void removeTreeModelListener(TreeModelListener l)
	{
		synchronized (_listeners) {
			_listeners.remove(l);
		}
	}

	@Override
	final public Object getRoot()
	{
		return _currentRoot;
	}

	@Override
	final public Object getChild(Object parent, int index)
	{
		return ((HistoryEventTreeNode) parent).children().get(index);
	}

	@Override
	final public int getChildCount(Object parent)
	{
		return ((HistoryEventTreeNode) parent).childCount();
	}

	@Override
	final public boolean isLeaf(Object node)
	{
		return getChildCount(node) == 0;
	}

	@Override
	final public int getIndexOfChild(Object parent, Object child)
	{
		List<HistoryEventTreeNode> children = ((HistoryEventTreeNode) parent).children();
		for (int lcv = 0; lcv < children.size(); lcv++)
			if (children.get(lcv) == child)
				return lcv;

		return -1;
	}

	@Override
	final public void valueForPathChanged(TreePath path, Object newValue)
	{
		// We don't allow changes.
	}

	private class HistoryEventFilterListenerImpl implements HistoryEventFilterListener
	{
		@Override
		public void filterChanged(HistoryEventFilter newFilter)
		{
			_currentRoot = HistoryEventTreeNode.formTree(_originalRoot, newFilter);
			fireTreeStructureChanged(_currentRoot, new Object[] { _currentRoot }, new int[] {}, new Object[] {});
		}
	}
}