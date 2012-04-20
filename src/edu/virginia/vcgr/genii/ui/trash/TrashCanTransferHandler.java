package edu.virginia.vcgr.genii.ui.trash;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.acls.ACLTransferable;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;
import edu.virginia.vcgr.genii.ui.rns.RNSTree;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeNode;
import edu.virginia.vcgr.genii.ui.rns.dnd.RNSListTransferData;
import edu.virginia.vcgr.genii.ui.rns.dnd.RNSListTransferable;

class TrashCanTransferHandler extends TransferHandler
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(TrashCanTransferHandler.class);
	
	private UIContext _uiContext;
	
	TrashCanTransferHandler(UIContext uiContext)
	{
	  _uiContext = uiContext;
	}
	
	@Override
	public boolean canImport(TransferSupport support)
	{
		Component comp = support.getComponent();
		
		if (comp instanceof TrashCanWidget)
		{
			// Accept only drag-and-drop, no cut/copy/paste
			if (!support.isDrop())
				return false;
			
			if (support.isDataFlavorSupported(ACLTransferable.DATA_FLAVOR))
			{
				support.setDropAction(MOVE);
				return true;
			}
			
			if (!support.isDataFlavorSupported(
				RNSListTransferable.RNS_PATH_LIST_FLAVOR))
				return false;
			
			boolean moveSupported = 
				(support.getSourceDropActions() & TransferHandler.MOVE) > 0;
			if (moveSupported)
			{
				support.setDropAction(TransferHandler.MOVE);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean importData(TransferSupport support)
	{
		try
		{
			Component comp = support.getComponent();
			Transferable t = support.getTransferable();
			
			if (comp instanceof TrashCanWidget)
			{
				 if (support.isDataFlavorSupported(
					ACLTransferable.DATA_FLAVOR) && support.getDropAction() == MOVE)
						return true;
				 
				RNSListTransferData data = (RNSListTransferData)t.getTransferData(
					RNSListTransferable.RNS_PATH_LIST_FLAVOR);
				_uiContext.progressMonitorFactory().createMonitor(
					comp, "Moving Entries to Trash",
					"Moving entries to trash.", 1000L,
					new RNSUnlinkedTask(data),
					new RNSUnlinkedCompletionListener(data.tree())).start();
				return true;
			}
		}
		catch (IOException ioe)
		{
			_logger.warn(
				"Unable to perform drag-and-drop or cut/copy/paste action.", 
				ioe);
			ErrorHandler.handleError(_uiContext,
				(JComponent)support.getComponent(), ioe);
		}
		catch (UnsupportedFlavorException e)
		{
			_logger.warn(
				"Unable to perform drag-and-drop or cut/copy/paste action.", 
				e);
			ErrorHandler.handleError(_uiContext,
				(JComponent)support.getComponent(), e);
		}
		
		return false;
	}
	
	private class RNSUnlinkResults
	{
		private Collection<RNSTreeNode> _successfulUnlinks;
		private Collection<Pair<RNSTreeNode, RNSPath>> _failedUnlinks;
		
		private RNSUnlinkResults(Collection<RNSTreeNode> successfulUnlinks,
			Collection<Pair<RNSTreeNode, RNSPath>> failedUnlinks)
		{
			_successfulUnlinks = successfulUnlinks;
			_failedUnlinks = failedUnlinks;
		}
	}
	
	private class RNSUnlinkedTask extends AbstractTask<RNSUnlinkResults>
	{
		private RNSListTransferData _transferData;
		
		private RNSUnlinkedTask(RNSListTransferData transferData)
		{
			_transferData = transferData;
		}
		
		@Override
		public RNSUnlinkResults execute(
				TaskProgressListener progressListener) throws Exception
		{
			Collection<RNSTreeNode> successes = new HashSet<RNSTreeNode>();
			Collection<Pair<RNSTreeNode, RNSPath>> failed = 
				new LinkedList<Pair<RNSTreeNode,RNSPath>>();
			
			IContextResolver resolver = ContextManager.getResolver();
			
			try
			{
				ContextManager.setResolver(
					new MemoryBasedContextResolver(
						_transferData.sourceContext().callingContext()));
				for (Pair<RNSTreeNode, RNSPath> pair : _transferData.paths())
				{
					RNSTreeNode node = pair.first();
					RNSPath path = pair.second();
					
					if (node == null)
					{
						_logger.warn(
							"Not allowed to trash root of RNS space.");
						
						JOptionPane.showMessageDialog(_transferData.tree(), 
							"Not permitted to trash the root of RNS space.", 
							"Root Unlink Attempted", JOptionPane.WARNING_MESSAGE);
						
						failed.add(pair);
					} else
					{
						try
						{
							_uiContext.trashCan().add(_transferData.sourceContext(), path);
							path.unlink();
							successes.add(node);
						}
						catch (Throwable cause)
						{
							if (wasCancelled())
								return null;
							
							_logger.warn(String.format(
								"Unable to unlink entry \"%s\".", path.pwd()),
								cause);
							ErrorHandler.handleError(
								_transferData.sourceContext(), _transferData.tree(), cause);
							failed.add(pair);
						}
					}
				}
				
				return new RNSUnlinkResults(successes, failed);
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
	
	private class RNSUnlinkedCompletionListener 
		implements TaskCompletionListener<RNSUnlinkResults>
	{
		private RNSTree _tree;
		
		private RNSUnlinkedCompletionListener(RNSTree tree)
		{
			_tree = tree;
		}
		
		@Override
		public void taskCancelled(Task<RNSUnlinkResults> task)
		{
			// Shouldn't happen.
		}

		@Override
		public void taskCompleted(
			Task<RNSUnlinkResults> task,
			RNSUnlinkResults result)
		{
			for (RNSTreeNode node : result._successfulUnlinks)
				node.refresh(_tree);
			
			if (!result._failedUnlinks.isEmpty())
				JOptionPane.showMessageDialog(
					_tree, "Unable to move all entries to the trash can.",
					"Trash Operation Failed", JOptionPane.WARNING_MESSAGE);
		}

		@Override
		public void taskExcepted(Task<RNSUnlinkResults> task, Throwable cause)
		{
			// Shouldn't happen.
		}
	}
}