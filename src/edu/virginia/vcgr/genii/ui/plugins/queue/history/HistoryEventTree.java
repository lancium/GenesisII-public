package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.Component;
import java.awt.Point;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.utils.hover.HoverDialogController;
import edu.virginia.vcgr.genii.ui.utils.hover.HoverDialogProvider;

class HistoryEventTree extends JTree
{
	static final long serialVersionUID = 0L;
	
	HistoryEventTree(UIContext context, Collection<HistoryEvent> events, 
		HistoryEventFilter filter)
	{
		super(new HistoryEventTreeModel(events, filter));
		
		setRootVisible(false);
		setShowsRootHandles(true);
		
		setCellRenderer(new HistoryEventTreeCellRenderer());
		
		new HoverDialogController(this,
			new HistoryEventDisplayDialogProvider(context));
	}
	
	private class HistoryEventDisplayDialogProvider
		implements HoverDialogProvider
	{
		private HistoryEventDisplayDialog _dialog;
		
		private HistoryEventDisplayDialogProvider(UIContext context)
		{
			_dialog = new HistoryEventDisplayDialog(SwingUtilities.getWindowAncestor(
				HistoryEventTree.this), context);
		}
		
		@Override
		public boolean updatePosition(Component sourceComponent, Point position)
		{
			TreePath path = getPathForLocation(position.x, position.y);
			if (path != null)
			{
				HistoryEventTreeNode node = 
					(HistoryEventTreeNode)path.getLastPathComponent();
				if (node != null)
				{
					_dialog.setEvent(node.event());
					return true;
				}
			}
			
			return false;
		}

		@Override
		public JDialog dialog()
		{
			return _dialog;
		}
	}
}