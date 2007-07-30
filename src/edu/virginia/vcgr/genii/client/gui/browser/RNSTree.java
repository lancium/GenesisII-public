package edu.virginia.vcgr.genii.client.gui.browser;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class RNSTree extends JTree implements
	TreeWillExpandListener, MouseListener
{
	static final long serialVersionUID = 0L;
	
	private ArrayList<IRNSPopupListener> _popupListeners =
		new ArrayList<IRNSPopupListener>();
	
	public RNSTree()
		throws RemoteException, 
			RNSPathDoesNotExistException, ConfigurationException
	{
		super(new RNSNode(RNSPath.getCurrent().getRoot()), true);
		
		addMouseListener(this);
		
		RNSNode root = (RNSNode)getModel().getRoot();
		root.preExpand(this);
		
		addTreeWillExpandListener(this);
		((DefaultTreeModel)getModel()).reload();
		
		setEditable(true);
		setCellEditor(new RNSCellEditor(this, 
			(DefaultTreeCellRenderer)getCellRenderer()));
	}
	
	public void addPopupListener(IRNSPopupListener listener)
	{
		synchronized(_popupListeners)
		{
			_popupListeners.add(listener);
		}
	}
	
	protected Collection<JMenuItem> firePopupsRequested(
		EndpointReferenceType target, TypeInformation targetInfo)
	{
		ArrayList<JMenuItem> popups = 
			new ArrayList<JMenuItem>();
		
		IRNSPopupListener []listeners;
		
		synchronized(_popupListeners)
		{
			listeners = new IRNSPopupListener[_popupListeners.size()];
			_popupListeners.toArray(listeners);
		}
		
		for (IRNSPopupListener listener : listeners)
		{
			listener.addPopupItems(target, targetInfo, popups);
		}
		
		return popups;
	}

	public void treeWillCollapse(TreeExpansionEvent arg0) 
		throws ExpandVetoException
	{
	}

	public void treeWillExpand(TreeExpansionEvent arg0)
		throws ExpandVetoException
	{
		try
		{
			((RNSNode)arg0.getPath().getLastPathComponent()).preExpand(
				(JTree)arg0.getSource());
		}
		catch (RemoteException re)
		{
			throw new ExpandVetoException(arg0, re.getLocalizedMessage());
		}
		catch (ConfigurationException ce)
		{
			throw new ExpandVetoException(arg0, ce.getLocalizedMessage());
		}
	}
	
	private JPopupMenu createMenu(EndpointReferenceType target,
		TypeInformation targetInfo)
	{
		boolean hasItems = false;
		JPopupMenu menu = new JPopupMenu("RNS Operations");
		
		if (targetInfo.isRNS())
		{
			hasItems = true;
			menu.add(new DefaultActions(DefaultActions.ACTION_NEW_FOLDER, this));
			menu.add(new DefaultActions(DefaultActions.ACTION_NEW_FILE, this));
			menu.addSeparator();
		}
	
		Collection<JMenuItem> menuItems =
			firePopupsRequested(target, targetInfo);
		if (menuItems.size() > 0)
		{
			hasItems = true;
			for (JMenuItem item : menuItems)
			{
				menu.add(item);
			}
			
			menu.addSeparator();
		}
		
		if (targetInfo.isRNS())
		{
			hasItems = true;
			menu.add(new DefaultActions(DefaultActions.ACTION_REFRESH, this));
		}
		
		return (hasItems ? menu : null);
	}
	
	static private class DefaultActions extends AbstractAction
	{
		private JTree _tree;
		static final long serialVersionUID = 0L;
		
		static public String ACTION_NEW_FOLDER = "Create New Directory";
		static public String ACTION_NEW_FILE = "Create New File";
		static public String ACTION_REFRESH = "Refresh";
		
		public DefaultActions(String action, JTree tree)
		{
			super(action);
			
			_tree = tree;
		}
		
		public void actionPerformed(ActionEvent arg0)
		{
			String actionCommand = arg0.getActionCommand();
			JMenuItem item = (JMenuItem)arg0.getSource();
			RNSNode node = (RNSNode)item.getClientProperty("RNSNode");
			
			try
			{
				if (actionCommand.equals(ACTION_NEW_FOLDER))
				{
					node.addDirectory(_tree, "New Folder");
				} else if (actionCommand.equals(ACTION_NEW_FILE))
				{	
					node.addFile(_tree, "New File");
				} else if (actionCommand.equals(ACTION_REFRESH))
				{
					node.refresh(_tree);
				}
			}
			catch (Throwable t)
			{
				t.printStackTrace(System.err);
			}
		}
	}

	public void mouseClicked(MouseEvent arg0)
	{
		if (arg0.isPopupTrigger())
			handlePopupRequest(arg0);
	}

	public void mouseEntered(MouseEvent arg0)
	{
	}

	public void mouseExited(MouseEvent arg0)
	{	
	}

	public void mousePressed(MouseEvent arg0)
	{	
	}

	public void mouseReleased(MouseEvent arg0)
	{
		if (arg0.isPopupTrigger())
			handlePopupRequest(arg0);
	}
	
	private void handlePopupRequest(MouseEvent event)
	{
		Point location = getPopupLocation(event);
		if (location == null)
			location = new Point(event.getX(), event.getY());
		
		TreePath path = getPathForLocation(event.getX(), event.getY());
		
		if (path != null)
		{
			RNSNode node = (RNSNode)path.getLastPathComponent();
			RNSEntry entry = (RNSEntry)node.getUserObject();
			JPopupMenu menu = createMenu(
				entry.getTarget(), entry.getTypeInformation());
			if (menu != null)
			{
				for (Component component : menu.getComponents())
				{
					if (component instanceof JMenuItem)
					{
						((JMenuItem)component).putClientProperty("RNSNode", node);
					}
				}
				
				menu.show(this, location.x, location.y);
			}
		}
	}
}