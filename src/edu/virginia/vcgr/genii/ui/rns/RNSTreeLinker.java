package edu.virginia.vcgr.genii.ui.rns;

import java.util.Collection;

import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

public class RNSTreeLinker extends RNSTreeOperator
{
	static private Log _logger = LogFactory.getLog(RNSTreeLinker.class);
	
	static public RNSTreeOperator link(RNSTree sourceTree,
		RNSTree targetTree, TreePath targetPath,
		UIContext sourceContext,
		Collection<Pair<RNSTreeNode, RNSPath>> paths)
	{
		return new RNSTreeLinker(sourceTree,
			targetTree, targetPath, sourceContext, paths);
	}
	
	private RNSTreeLinker(RNSTree sourceTree,
		RNSTree targetTree, TreePath targetPath,
		UIContext sourceContext,
		Collection<Pair<RNSTreeNode, RNSPath>> paths)
	{
		super(sourceContext, targetTree, targetPath,
			new RNSTreeOperatorSource(sourceTree, paths));
	}

	@Override
	public boolean performOperation()
	{
		_logger.debug("RNSTreeLinker called.");
		
		_uiContext.progressMonitorFactory().createMonitor(
			_targetTree, "Linking Endpoints", "Linking endpoints.",
			1000L, new LinkerTask(), null).start();
		return true;
	}
	
	private class LinkerTask extends AbstractTask<Integer>
	{
		@Override
		public Integer execute(TaskProgressListener progressListener)
				throws Exception
		{
			RNSTreeNode targetParentNode = (RNSTreeNode)_targetPath.getLastPathComponent();
			RNSFilledInTreeObject targetParentObject = 
				(RNSFilledInTreeObject)targetParentNode.getUserObject();
			
			if (_sourceInformation.isRNSSource())
			{
				RNSTreeOperatorSource source = (RNSTreeOperatorSource)_sourceInformation;
				
				for (Pair<RNSTreeNode, RNSPath> path : source.sourcePaths())
				{
					progressListener.updateSubTitle(String.format("Linking %s", 
						path.second().getName()));
					RNSPath target = getTargetPath(
						targetParentObject.path(), path.second().getName());
					if (target != null)
					{
						IContextResolver resolver = ContextManager.getResolver();
						
						try
						{
							ContextManager.setResolver(
								new MemoryBasedContextResolver(
									_uiContext.callingContext()));
							target.link(path.second().getEndpoint());
							new RefreshWorker(_targetTree, targetParentNode).run();
						}
						catch (Throwable cause)
						{
							if (wasCancelled())
								return null;
							
							_logger.warn(String.format(
								"Unable to create link for source \"%s\".", 
								path.second().pwd()), cause);
							ErrorHandler.handleError(
								_uiContext, _targetTree, cause);
						}
						finally
						{
							ContextManager.setResolver(resolver);
						}
					}
				}
			}
			
			return null;
		}

		@Override
		public boolean showProgressDialog()
		{
			return true;
		}
	}
}