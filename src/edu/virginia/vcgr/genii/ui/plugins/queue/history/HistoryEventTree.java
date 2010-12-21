package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.Color;
import java.awt.Point;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.ui.UIContext;

class HistoryEventTree extends JTree
{
	static final long serialVersionUID = 0L;
	
	private class RightClickListener extends MouseAdapter
	{
		@Override
		final public void mouseClicked(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e.getPoint());
		}

		@Override
		final public void mousePressed(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e.getPoint());
		}

		@Override
		final public void mouseReleased(MouseEvent e)
		{
			if (e.isPopupTrigger())
				popup(e.getPoint());
		}
	}
	
	private class DisplayEventDetailsAction extends AbstractAction
	{
		static final long serialVersionUID = 0l;
		
		private HistoryEvent _event;
		
		private DisplayEventDetailsAction(HistoryEvent event)
		{
			super("Event Details");
			
			_event = event;
			setEnabled(_event != null);
		}
		
		@Override
		final public void actionPerformed(ActionEvent e)
		{
			HistoryEventDisplayDialog dialog;
			dialog = new HistoryEventDisplayDialog(SwingUtilities.getWindowAncestor(
				HistoryEventTree.this), _context);
			dialog.setEvent(_event);
			
			dialog.pack();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setModalityType(ModalityType.MODELESS);
			dialog.setLocationRelativeTo(HistoryEventTree.this);
			dialog.setVisible(true);
		}
	}
	
	private void popup(Point location)
	{
		HistoryEvent event = null;
		
		TreePath path = getPathForLocation(location.x, location.y);
		if (path != null)
		{
			HistoryEventTreeNode node = 
				(HistoryEventTreeNode)path.getLastPathComponent();
			if (node != null)
				event = node.event();
		}
		
		JPopupMenu menu = new JPopupMenu("History Event Popup");
		menu.add(new DisplayEventDetailsAction(event));
		
		menu.show(this, location.x, location.y);
	}
	
	private UIContext _context;
	
	HistoryEventTree(UIContext context, Collection<HistoryEvent> events, 
		HistoryEventFilter filter)
	{
		super(new HistoryEventTreeModel(events, filter));
		
		setBorder(new AttemptNumberBorder(new Color(225, 255, 225),
			Color.black, null));
		_context = context;
		
		setRootVisible(false);
		setShowsRootHandles(true);
		
		setCellRenderer(new HistoryEventTreeCellRenderer());
		
		addMouseListener(new RightClickListener());
		
		/*
		new HoverDialogController(this,
			new HistoryEventDisplayDialogProvider(context));
		*/
	}
	
//	private class HistoryEventDisplayDialogProvider
//		implements HoverDialogProvider
//	{
//		private HistoryEventDisplayDialog _dialog;
//		
//		private HistoryEventDisplayDialogProvider(UIContext context)
//		{
//			_dialog = new HistoryEventDisplayDialog(SwingUtilities.getWindowAncestor(
//				HistoryEventTree.this), context);
//		}
//		
//		@Override
//		public boolean updatePosition(Component sourceComponent, Point position)
//		{
//			TreePath path = getPathForLocation(position.x, position.y);
//			if (path != null)
//			{
//				HistoryEventTreeNode node = 
//					(HistoryEventTreeNode)path.getLastPathComponent();
//				if (node != null)
//				{
//					_dialog.setEvent(node.event());
//					return true;
//				}
//			}
//			
//			return false;
//		}
//
//		@Override
//		public JDialog dialog()
//		{
//			return _dialog;
//		}
//	}
}