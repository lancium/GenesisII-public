package edu.virginia.vcgr.genii.client.gui.widgets.rns;

import java.util.HashSet;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class RNSTreeModel extends DefaultTreeModel 
	implements TreeWillExpandListener, TreeExpansionListener
{
	static final long serialVersionUID = 0L;
	private Thread _backgroundThread;
	private HashSet<RNSTreeNode> _expandedNodes = new HashSet<RNSTreeNode>();
	private RNSTree _tree;	
	
	public RNSTreeModel(RNSPath rootPath) throws RNSPathDoesNotExistException
	{
		super(new RNSTreeNode(rootPath), true);
		
		_tree = null;
		
		_expandedNodes.add((RNSTreeNode)getRoot());
		
		_backgroundThread = new Thread(new BackgroundUpdater(this));
		_backgroundThread.setPriority(Thread.NORM_PRIORITY - 2);
		_backgroundThread.setDaemon(false);
		_backgroundThread.setName("RNSTreeModel Background Thread.");
		_backgroundThread.start();
	}
	
	public void setTree(RNSTree tree)
	{
		_tree = tree;
	}
	
	synchronized protected void finalize()
	{
		if (_backgroundThread != null)
		{
			_backgroundThread.interrupt();
			_backgroundThread = null;
		}
	}
	
	@Override
	public void treeWillCollapse(TreeExpansionEvent event)
			throws ExpandVetoException
	{
	}

	@Override
	public void treeWillExpand(TreeExpansionEvent event)
			throws ExpandVetoException
	{
		RNSTreeNode node;

		node = (RNSTreeNode)(event.getPath().getLastPathComponent());
		if (!prepareExpansion(node))
			throw new ExpandVetoException(event);
	}
	
	public boolean prepareExpansion(RNSTreeNode node)
	{
		if (!node.getAllowsChildren())
			return false;
		
		node.prepareExpansion(this);
		
		return true;
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event)
	{
		synchronized(_expandedNodes)
		{
			_expandedNodes.remove(event.getPath().getLastPathComponent());
		}
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event)
	{
		synchronized(_expandedNodes)
		{
			_expandedNodes.add(
				(RNSTreeNode)event.getPath().getLastPathComponent());
		}
	}
	
	private class BackgroundUpdater implements Runnable
	{
		private RNSTreeModel _model;
		
		public BackgroundUpdater(RNSTreeModel model)
		{
			_model = model;
		}
		
		public void run()
		{
			try
			{
				while (true)
				{
					Thread.sleep(1000 * 5);
					
					RNSTreeNode []expanded;
					synchronized(_expandedNodes)
					{
						expanded = _expandedNodes.toArray(new RNSTreeNode[0]);
					}
					
					for (RNSTreeNode node : expanded)
					{
						if (!_tree.isExpanded(new TreePath(node.getPath())))
						{
							synchronized(_expandedNodes)
							{
								_expandedNodes.remove(node);
							}
						} else
						{
							node.prepareExpansion(_model);
						}
					}
				}
			}
			catch (InterruptedException ie)
			{
				return;
			}
		}
	}
}