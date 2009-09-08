package edu.virginia.vcgr.genii.ui.rns;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

public class RNSTreeNode extends DefaultMutableTreeNode
{
	static final long serialVersionUID = 0L;
	
	static final private int DEFAULT_GC_WINDOW = 1000 * 16;
	
	private GCListener _gcListener = new GCListener();
	private RNSTreeNodeState _nodeState;
	private Timer _gcTimer = null;
	
	RNSTreeNode(RNSTreeNode original)
	{
		super(original.getUserObject(), original.allowsChildren);
		_nodeState = original._nodeState;
		
		if (original.allowsChildren)
		{
			for (Object obj : original.children)
				add(new RNSTreeNode((RNSTreeNode)obj));
		}
		
		if (original._gcTimer != null)
			_gcTimer = new Timer(DEFAULT_GC_WINDOW, _gcListener);
	}
	
	public RNSTreeNode(RNSTreeObject object)
	{
		super(object, object.allowsChildren());
		
		if (allowsChildren)
		{
			add(new RNSTreeNode(DefaultRNSTreeObject.createExpandingObject()));
			_nodeState = RNSTreeNodeState.NEEDS_EXPANSION;
		} else
			_nodeState = RNSTreeNodeState.EXPANDED;
	}
	
	RNSTreeNode lookup(String name)
	{
		for (Object child : children)
		{
			if (child.toString().equals(name))
				return (RNSTreeNode)child;
		}
		
		return null;
	}
	
	RNSTreeNodeState nodeState()
	{
		return _nodeState;
	}
	
	void collapse()
	{
		if (_gcTimer != null)
			_gcTimer.stop();
		_gcTimer = new Timer(DEFAULT_GC_WINDOW, _gcListener);
		_gcTimer.setRepeats(false);
		_gcTimer.start();
	}
	
	void noteExpansion()
	{
		if (_gcTimer != null)
			_gcTimer.stop();
		_gcTimer = null;
	}
	
	void expand(RNSTree tree)
	{
		if (_nodeState == RNSTreeNodeState.EXPANDING)
			return;
		
		_nodeState = RNSTreeNodeState.EXPANDING;
		
		RNSTreeModel model = (RNSTreeModel)tree.getModel();
		model.reload(RNSTreeNode.this);
		
		RNSFilledInTreeObject object = (RNSFilledInTreeObject)getUserObject();
		model.uiContext().progressMonitorFactory().monitor(tree, null, null,
			1000L * 1000, new ExpansionTask(
				model.uiContext().callingContext(), object.path()), 
				new ExpansionCompletionListener(tree, model.uiContext()));
	}
	
	public void refresh(RNSTree tree)
	{
		expand(tree);
	}
	
	private class ExpansionTask extends AbstractTask<RNSPath[]>
	{
		private RNSPath _parent;
		private ICallingContext _context;
		
		private ExpansionTask(ICallingContext context,
			RNSPath parent)
		{
			_context = context;
			_parent = parent;
		}
		
		@Override
		public RNSPath[] execute(TaskProgressListener progressListener)
				throws Exception
		{
			IContextResolver resolver = ContextManager.getResolver();
			
			try
			{
				ContextManager.setResolver(
					new MemoryBasedContextResolver(_context));
				return _parent.listContents().toArray(new RNSPath[0]);
			}
			finally
			{
				ContextManager.setResolver(resolver);
			}
		}

		@Override
		public boolean showProgressDialog()
		{
			return false;
		}
	}
	
	private class ExpansionCompletionListener 
		implements TaskCompletionListener<RNSPath[]>
	{
		private UIContext _context;
		private RNSTree _tree;
		
		private ExpansionCompletionListener(RNSTree tree, UIContext context)
		{
			_context = context;
			_tree = tree;
		}
		
		@Override
		public void taskCancelled(Task<RNSPath[]> task)
		{
			// This shouldn't happen -- no cancels allowed.
		}

		@Override
		public void taskCompleted(Task<RNSPath[]> task, RNSPath[] result)
		{
			RNSTreeModel model = (RNSTreeModel)_tree.getModel();
			
			removeAllChildren();
			for (RNSPath entry : result)
			{
				try
				{
					add(new RNSTreeNode(new RNSFilledInTreeObject(entry)));
					_nodeState = RNSTreeNodeState.EXPANDED;
					noteExpansion();
				}
				catch (RNSPathDoesNotExistException e)
				{
					ErrorHandler.handleError(_context, _tree, e);
				}
			}
			
			model.reload(RNSTreeNode.this);
		}

		@Override
		public void taskExcepted(Task<RNSPath[]> task, Throwable cause)
		{
			RNSTreeModel model = (RNSTreeModel)_tree.getModel();
			
			removeAllChildren();
			add(new RNSTreeNode(DefaultRNSTreeObject.createErrorObject()));
			_nodeState = RNSTreeNodeState.EXPANDED;
			noteExpansion();
			
			model.reload(RNSTreeNode.this);

			ErrorHandler.handleError(_context, _tree, cause);
		}
	}
	
	private class GCListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			removeAllChildren();
			add(new RNSTreeNode(DefaultRNSTreeObject.createExpandingObject()));
			_nodeState = RNSTreeNodeState.NEEDS_EXPANSION;
		}
	}
}