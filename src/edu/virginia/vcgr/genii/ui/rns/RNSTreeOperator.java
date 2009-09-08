package edu.virginia.vcgr.genii.ui.rns;

import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.ui.UIContext;

public abstract class RNSTreeOperator
{
	protected UIContext _uiContext;
	
	protected RNSTree _targetTree;	
	protected TreePath _targetPath;
	
	protected OperatorSource _sourceInformation;
	
	protected RNSTreeOperator(UIContext uiContext,
		RNSTree targetTree, TreePath targetPath,
		OperatorSource sourceInformation)
	{
		_uiContext = uiContext;
		_targetTree = targetTree;
		_targetPath = targetPath;
		
		_sourceInformation = sourceInformation;
	}
	
	protected RNSPath getTargetPath(RNSPath parentPath, String originalName)
	{
		try
		{
			RNSPath ret = parentPath.lookup(originalName, RNSPathQueryFlags.DONT_CARE);
			if (!ret.exists())
				return ret;
			
			ret = parentPath.lookup(String.format("Copy of %s", originalName),
				RNSPathQueryFlags.DONT_CARE);
			if (!ret.exists())
				return ret;
			
			int lcv = 2;
			while (true)
			{
				ret = parentPath.lookup(String.format(
					"Copy of %s (%d)", originalName, lcv++), 
					RNSPathQueryFlags.DONT_CARE);
				if (!ret.exists())
					return ret;
			}
		}
		catch (RNSPathAlreadyExistsException rpaee)
		{
			// Shouldn't happen
		}
		catch (RNSPathDoesNotExistException e)
		{
			// Shouldn't happen
		}
		
		return null;
	}
	
	public abstract boolean performOperation();
	
	static protected class RefreshWorker implements Runnable
	{
		private RNSTree _tree;
		private RNSTreeNode _node;
		
		protected RefreshWorker(RNSTree tree, RNSTreeNode node)
		{
			_tree = tree;
			_node = node;
		}
		
		@Override
		public void run()
		{
			if (!SwingUtilities.isEventDispatchThread())
			{
				SwingUtilities.invokeLater(this);
				return;
			}
			
			_node.refresh(_tree);
		}
	}
}