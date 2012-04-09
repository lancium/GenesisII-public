package edu.virginia.vcgr.genii.ui.rns;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DropMode;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.morgan.util.Pair;
import org.morgan.utils.gui.tearoff.TearoffHandler;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.EndpointRetriever;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;
import edu.virginia.vcgr.genii.ui.rns.dnd.RNSTransferHandler;
import edu.virginia.vcgr.genii.ui.utils.CommonKeyStrokes;

public class RNSTree extends JTree implements EndpointRetriever
{
	static final long serialVersionUID = 0L;
	
	static final public Dimension DESIRED_BROWSER_SIZE = new Dimension(
		300, 300);
	
	private Collection<RNSTreeListener> _listeners = new LinkedList<RNSTreeListener>();
	
	private void setupInputMap(InputMap iMap)
	{
		iMap.put(CommonKeyStrokes.REFRESH,
			_refreshAction.getValue(Action.NAME));
		iMap.put(CommonKeyStrokes.BACKSPACE,
			_deleteAction.getValue(Action.NAME));
		iMap.put(CommonKeyStrokes.DELETE,
			_deleteAction.getValue(Action.NAME));
		/* for now, we ignore cut/copy/paste */
		/*
		iMap.put(KeyStroke.getKeyStroke("ctrl X"),
	        TransferHandler.getCutAction().getValue(Action.NAME));
		iMap.put(KeyStroke.getKeyStroke("ctrl C"),
	        TransferHandler.getCopyAction().getValue(Action.NAME));
	    */
	}
	
	private void setupActionMap(ActionMap aMap)
	{
		aMap.put(_refreshAction.getValue(Action.NAME), _refreshAction);
		aMap.put(_deleteAction.getValue(Action.NAME), _deleteAction);
		/* for now, we ignore cut/copy/paste */
		/*
		aMap.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
        aMap.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        */
	}
	
	private RefreshAction _refreshAction = new RefreshAction();
	private DeleteAction _deleteAction = new DeleteAction();
	
	private void checkPathExpansion(RNSTree original,
		TreePath originalPath, TreePath newPath)
	{
		if (original.isExpanded(originalPath))
		{
			setExpandedState(newPath, true);
			
			RNSTreeNode originalNode = 
				(RNSTreeNode)originalPath.getLastPathComponent();
			RNSTreeNode newNode =
				(RNSTreeNode)newPath.getLastPathComponent();
			
			int size = originalNode.getChildCount();
			for (int lcv = 0; lcv < size; lcv++)
			{
				RNSTreeNode originalChild = (RNSTreeNode)originalNode.getChildAt(lcv);
				RNSTreeNode newChild = (RNSTreeNode)newNode.getChildAt(lcv);
				checkPathExpansion(original, 
					originalPath.pathByAddingChild(originalChild), 
					newPath.pathByAddingChild(newChild));
			}
		}
	}
	
	private DirectoryChangeListenerImpl _dChangeListener =
		new DirectoryChangeListenerImpl();
	
	private RNSTree(RNSTreeModel model)
	{
		super(model);
		
		addRNSTreeListener(new DefaultDoubleClickListener());
		
		DefaultTreeSelectionModel selectionModel =
			new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);
		setSelectionModel(selectionModel);
		
		setCellRenderer(new RNSTreeCellRenderer(model.appContext()));
		setAutoscrolls(true);
		setEditable(false);
		setShowsRootHandles(true);
		setExpandedState(
			new TreePath(((RNSTreeNode)model.getRoot()).getPath()), false);
		
		addTreeWillExpandListener(new TreeWillExpandListenerImpl());
		
		setupInputMap(getInputMap());
		setupActionMap(getActionMap());
		
		setTransferHandler(new RNSTransferHandler(model.uiContext()));
		setDragEnabled(true);
		setDropMode(DropMode.ON_OR_INSERT);
		
		addAncestorListener(new AncestorListenerImpl());
		
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int selRow = getRowForLocation(e.getX(), e.getY());
				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				if(selRow != -1)
				{
					if(e.getClickCount() == 1)
						fireRNSTreePathClicked(selRow, selPath);
					else if(e.getClickCount() == 2)
						fireRNSTreePathDoubleClicked(selRow, selPath);
				}
			}
		});
	}
	
	private void fireRNSTreePathClicked(RNSFilledInTreeObject fObj)
	{
		RNSTreeModel model = (RNSTreeModel)getModel();
		
		Collection<RNSTreeListener> listeners;
		
		synchronized(_listeners)
		{
			listeners = new ArrayList<RNSTreeListener>(_listeners);
		}
		
		for (RNSTreeListener listener : listeners)
			listener.pathClicked(this, model.uiContext(), fObj);
	}
	
	private void fireRNSTreePathDoubleClicked(RNSFilledInTreeObject fObj)
	{
		RNSTreeModel model = (RNSTreeModel)getModel();
		
		Collection<RNSTreeListener> listeners;
		
		synchronized(_listeners)
		{
			listeners = new ArrayList<RNSTreeListener>(_listeners);
		}
		
		for (RNSTreeListener listener : listeners)
			listener.pathDoubleClicked(this, model.uiContext(), fObj);
	}
	
	private void fireRNSTreePathClicked(int row, TreePath path)
	{
		RNSTreeNode node = (RNSTreeNode)(path.getLastPathComponent());
		RNSTreeObject obj = (RNSTreeObject)node.getUserObject();
		if (obj.objectType() == RNSTreeObjectType.ENDPOINT_OBJECT)
		{
			RNSFilledInTreeObject fObj = (RNSFilledInTreeObject)obj;
			fireRNSTreePathClicked(fObj);
		}
	}
	
	private void fireRNSTreePathDoubleClicked(int row, TreePath path)
	{
		RNSTreeNode node = (RNSTreeNode)(path.getLastPathComponent());
		RNSTreeObject obj = (RNSTreeObject)node.getUserObject();
		if (obj.objectType() == RNSTreeObjectType.ENDPOINT_OBJECT)
		{
			RNSFilledInTreeObject fObj = (RNSFilledInTreeObject)obj;
			fireRNSTreePathDoubleClicked(fObj);
		}
	}
	
	private RNSTree(RNSTree original)
	{
		this(new RNSTreeModel((RNSTreeModel)original.getModel()));
		
		TreePath originalPath = new TreePath(original.getModel().getRoot());
		TreePath newPath = new TreePath(getModel().getRoot());
		checkPathExpansion(original, originalPath, newPath);
	}
	
	public RNSTree(ApplicationContext appContext, 
		UIContext uiContext) throws RNSPathDoesNotExistException
	{
		this(new RNSTreeModel(appContext, uiContext));
	}
	
	public void addRNSTreeListener(RNSTreeListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(listener);
		}
	}
	
	public void removeRNSTreeListener(RNSTreeListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(listener);
		}
	}
	
	public TearoffHandler createTearoffHandler(ApplicationContext appContext)
	{
		return new TearoffHandlerImpl(appContext);
	}
	
	public void deletePaths(TreePath []paths)
	{
		if (paths == null || paths.length == 0)
			return;
		
		RNSTreeModel model = (RNSTreeModel)getModel();
		Collection<Pair<RNSTreeNode, RNSPath>> pathList =
			new Vector<Pair<RNSTreeNode,RNSPath>>();
		
		for (TreePath tp : paths)
		{
			RNSTreeNode node = (RNSTreeNode)tp.getLastPathComponent();
			if (node != null)
			{
				RNSTreeObject obj = (RNSTreeObject)node.getUserObject();
				if (obj != null && obj.objectType()
					== RNSTreeObjectType.ENDPOINT_OBJECT)
				{
					pathList.add(new Pair<RNSTreeNode, RNSPath>(node,
						((RNSFilledInTreeObject)obj).path()));
				}
			}
		}
		
		model.uiContext().progressMonitorFactory().createMonitor(
			this, "Delete Entries", "Deleting entries",
			1000L, new DeleteTask(model.uiContext(), pathList), null).start();
	}
	
	private class TearoffHandlerImpl implements TearoffHandler
	{
		private ApplicationContext _applicationContext;
		
		private TearoffHandlerImpl(ApplicationContext applicationContext)
		{
			_applicationContext = applicationContext;
		}
		
		@Override
		public Window tearoff(JComponent arg0)
		{
			RNSTreeModel model = (RNSTreeModel)getModel();
			UIContext context = (UIContext)model.uiContext().clone();
			
			RNSTree newTree = new RNSTree(RNSTree.this);
			Window ret = new RNSBrowserTearoffWindow(
				_applicationContext, context, newTree);	

			Container parent = getParent();
			if (parent instanceof JViewport)
			{
				Point p = ((JViewport)parent).getViewPosition();
				TreePath path = getClosestPathForLocation(p.x, p.y);
				if (path != null)
				{
					TreePath newPath =
						((RNSTreeModel)newTree.getModel()).translatePath(path);
					if (newPath != null)
						newTree.scrollPathToVisible(newPath);
				}
			}
			
			return ret;
		}
	}
	
	private Map<String, RNSTreeNode> _expandedNodes =
		new HashMap<String, RNSTreeNode>();
	
	private class TreeWillExpandListenerImpl implements TreeWillExpandListener
	{
		@Override
		public void treeWillCollapse(TreeExpansionEvent event)
				throws ExpandVetoException
		{
			TreePath path = event.getPath();
			RNSTreeNode node = (RNSTreeNode)path.getLastPathComponent();
			RNSTreeObject obj = (RNSTreeObject)node.getUserObject();
			if (obj.objectType() == RNSTreeObjectType.ENDPOINT_OBJECT)
				_expandedNodes.remove(
					((RNSFilledInTreeObject)obj).path().pwd());
			node.collapse();
		}

		@Override
		public void treeWillExpand(TreeExpansionEvent event)
				throws ExpandVetoException
		{
			TreePath path = event.getPath();
			RNSTreeNode node = (RNSTreeNode)path.getLastPathComponent();
			RNSTreeObject obj = (RNSTreeObject)node.getUserObject();
			if (obj.objectType() == RNSTreeObjectType.ENDPOINT_OBJECT)
				_expandedNodes.put(
					((RNSFilledInTreeObject)obj).path().pwd(), node);
			node.noteExpansion();
			if (node.nodeState() == RNSTreeNodeState.NEEDS_EXPANSION)
				node.expand(RNSTree.this);
		}
	}
	
	private class DirectoryChangeListenerImpl implements DirectoryChangeListener
	{
		@Override
		public void contentsChanged(String parentDirectory)
		{
			RNSTreeNode node = _expandedNodes.get(parentDirectory);
			if (node != null)
				node.refresh(RNSTree.this);
		}
	}
	
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
			TreePath []paths = getSelectionPaths();
			if (paths == null)
				return;
			
			for (TreePath path : getSelectionPaths())
				((RNSTreeNode)path.getLastPathComponent()).refresh(
					RNSTree.this);
		}
	}
	
	private class DeleteAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private DeleteAction()
		{
			super("Delete");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			TreePath []paths = getSelectionPaths();
			if (paths != null && paths.length > 0)
			{
				int answer;
				
				if (paths.length == 1)
					answer = JOptionPane.showConfirmDialog(RNSTree.this,
						String.format(
							"Are you sure you wish to delete \"%s\"?", 
							paths[0].getLastPathComponent()), 
						"Delete Confirmation", JOptionPane.YES_NO_OPTION);
				else
					answer = JOptionPane.showConfirmDialog(RNSTree.this,
						"Are you sure you wish to delete the selected endpoints?",
						"Delete Confirmation", JOptionPane.YES_NO_OPTION);
				
				if (answer == JOptionPane.YES_OPTION)
					deletePaths(paths);
			}
		}
	}
	
	private class DeleteTask extends AbstractTask<Integer>
	{
		private UIContext _sourceContext;
		private Collection<Pair<RNSTreeNode, RNSPath>> _paths;
		
		private DeleteTask(UIContext sourceContext, 
			Collection<Pair<RNSTreeNode, RNSPath>> paths)
		{
			_sourceContext = sourceContext;
			_paths = paths;
		}
		
		@Override
		public Integer execute(TaskProgressListener progressListener)
				throws Exception
		{
			IContextResolver resolver = ContextManager.getResolver();
			
			try
			{
				ContextManager.setResolver(
					new MemoryBasedContextResolver(
						_sourceContext.callingContext()));

				for (Pair<RNSTreeNode, RNSPath> path : _paths)
				{
					_sourceContext.trashCan().add(
						_sourceContext, path.second());
					path.second().unlink();
					path.first().refresh(RNSTree.this);
				}
				
				return null;
			}
			catch (Throwable cause)
			{
				if (wasCancelled())
					return null;
				
				ErrorHandler.handleError(_sourceContext, RNSTree.this, cause);
			}
			finally
			{
				ContextManager.setResolver(resolver);
			}
			
			return null;
		}

		@Override
		public boolean showProgressDialog()
		{
			return true;
		}
	}
	
	private class AncestorListenerImpl implements AncestorListener
	{
		@Override
		public void ancestorAdded(AncestorEvent event)
		{
			RNSTreeModel model = (RNSTreeModel)getModel();
			
			model.uiContext().directoryChangeNexus().addDirectoryChangeListener(
				_dChangeListener);
		}

		@Override
		public void ancestorMoved(AncestorEvent event)
		{
			// Do nothing
		}

		@Override
		public void ancestorRemoved(AncestorEvent event)
		{
			RNSTreeModel model = (RNSTreeModel)getModel();
			
			model.uiContext().directoryChangeNexus().removeDirectoryChangeListener(
				_dChangeListener);
		}
	}
	
	@Override
	public Collection<RNSPath> getTargetEndpoints()
	{
		Collection<RNSPath> endpoints = new LinkedList<RNSPath>();
		
		TreePath []selected = getSelectionPaths();
		if (selected != null)
		{
			for (TreePath s : selected)
			{
				RNSTreeNode node = (RNSTreeNode)s.getLastPathComponent();
				RNSTreeObject obj = (RNSTreeObject)node.getUserObject();
				if (obj.objectType() == RNSTreeObjectType.ENDPOINT_OBJECT)
				{
					RNSFilledInTreeObject fObj = (RNSFilledInTreeObject)obj;
					endpoints.add(fObj.path());
				}
			}
		}
		
		return endpoints;
	}
	
	@Override
	public void refresh()
	{
		TreePath []paths = getSelectionPaths();
		if (paths != null)
		{
			for (TreePath path : paths)
				((RNSTreeNode)path.getLastPathComponent()).refresh(this);
		}
	}

	@Override
	public void refreshParent()
	{
		TreePath []paths = getSelectionPaths();
		if (paths != null)
		{
			for (TreePath path : paths)
			{
				path = path.getParentPath();
				if (path != null)
				{
					((RNSTreeNode)path.getLastPathComponent()).refresh(this);
				}
			}
		}
	}
}