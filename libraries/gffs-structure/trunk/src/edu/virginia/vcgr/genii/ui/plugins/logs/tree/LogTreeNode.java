package edu.virginia.vcgr.genii.ui.plugins.logs.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

public class LogTreeNode extends DefaultMutableTreeNode {
	static final long serialVersionUID = 0L;

	static final private int DEFAULT_GC_WINDOW = 1000 * 16;

	private GCListener _gcListener = new GCListener();
	private LogTreeNodeState _nodeState;
	private Timer _gcTimer = null;

	LogTreeNode(LogTreeNode original) {
		super(original.getUserObject(), original.allowsChildren);
		_nodeState = original._nodeState;

		if (original.allowsChildren) {
			for (Object obj : original.children)
				add(new LogTreeNode((LogTreeNode) obj));
		}

		if (original._gcTimer != null)
			_gcTimer = new Timer(DEFAULT_GC_WINDOW, _gcListener);
	}

	public LogTreeNode(LogTreeObject object) {
		super(object, object.allowsChildren());

		if (allowsChildren) {
			add(new LogTreeNode(DefaultLogTreeObject.createExpandingObject()));
			_nodeState = LogTreeNodeState.NEEDS_EXPANSION;
		} else
			_nodeState = LogTreeNodeState.EXPANDED;
	}

	LogTreeNode lookup(String name) {
		for (Object child : children) {
			if (child.toString().equals(name))
				return (LogTreeNode) child;
		}

		return null;
	}

	LogTreeNodeState nodeState() {
		return _nodeState;
	}

	void collapse() {
		if (_gcTimer != null)
			_gcTimer.stop();
		_gcTimer = new Timer(DEFAULT_GC_WINDOW, _gcListener);
		_gcTimer.setRepeats(false);
		_gcTimer.start();
	}

	void noteExpansion() {
		if (_gcTimer != null)
			_gcTimer.stop();
		_gcTimer = null;
	}

	void expand(LogTree tree, DisplayByType type) {
		if (_nodeState == LogTreeNodeState.EXPANDING)
			return;

		_nodeState = LogTreeNodeState.EXPANDING;

		LogTreeModel model = (LogTreeModel) tree.getModel();
		model.reload(LogTreeNode.this);

		LogFilledInTreeObject object = (LogFilledInTreeObject) getUserObject();
		object.path().setDisplayType(type);
		model.uiPluginContext()
				.uiContext()
				.progressMonitorFactory()
				.createMonitor(
						tree,
						null,
						null,
						1000L * 1000,
						new ExpansionTask(model.uiPluginContext().uiContext()
								.callingContext(), object.path()),
						new ExpansionCompletionListener(tree, model
								.uiPluginContext().uiContext())).start();
	}

	public void refresh(LogTree tree, DisplayByType type) {
		expand(tree, type);
	}

	private class ExpansionTask extends AbstractTask<LogPath[]> {
		private LogPath _parent;
		private ICallingContext _context;

		private ExpansionTask(ICallingContext context, LogPath logPath) {
			_context = context;
			_parent = logPath;
		}

		@Override
		public LogPath[] execute(TaskProgressListener progressListener)
				throws Exception {
			IContextResolver resolver = ContextManager.getResolver();

			try {
				ContextManager.setResolver(new MemoryBasedContextResolver(
						_context));
				return _parent.listContents().toArray(new LogPath[0]);
			} finally {
				ContextManager.setResolver(resolver);
			}
		}

		@Override
		public boolean showProgressDialog() {
			return false;
		}
	}

	private class ExpansionCompletionListener implements
			TaskCompletionListener<LogPath[]> {
		private UIContext _context;
		private LogTree _tree;

		private ExpansionCompletionListener(LogTree tree, UIContext context) {
			_context = context;
			_tree = tree;
		}

		@Override
		public void taskCancelled(Task<LogPath[]> task) {
			// This shouldn't happen -- no cancels allowed.
		}

		@Override
		public void taskCompleted(Task<LogPath[]> task, LogPath[] result) {
			LogTreeModel model = (LogTreeModel) _tree.getModel();

			removeAllChildren();
			for (LogPath entry : result) {
				try {
					add(new LogTreeNode(new LogFilledInTreeObject(entry)));
					_nodeState = LogTreeNodeState.EXPANDED;
					noteExpansion();
				} catch (Throwable e) {
					ErrorHandler.handleError(_context, _tree, e);
				}
			}

			model.reload(LogTreeNode.this);
		}

		@Override
		public void taskExcepted(Task<LogPath[]> task, Throwable cause) {
			LogTreeModel model = (LogTreeModel) _tree.getModel();

			removeAllChildren();
			add(new LogTreeNode(DefaultLogTreeObject.createErrorObject()));
			_nodeState = LogTreeNodeState.EXPANDED;
			noteExpansion();

			model.reload(LogTreeNode.this);

			ErrorHandler.handleError(_context, _tree, cause);
		}
	}

	private class GCListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			removeAllChildren();
			add(new LogTreeNode(DefaultLogTreeObject.createExpandingObject()));
			_nodeState = LogTreeNodeState.NEEDS_EXPANSION;
		}
	}
}