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

public class RNSTreeMover extends RNSTreeOperator
{
	static private Log _logger = LogFactory.getLog(RNSTreeMover.class);

	static public RNSTreeOperator move(RNSTree sourceTree, RNSTree targetTree, TreePath targetPath, UIContext sourceContext,
		Collection<Pair<RNSTreeNode, RNSPath>> paths)
	{
		return new RNSTreeMover(sourceTree, targetTree, targetPath, sourceContext, paths);
	}

	private RNSTreeMover(RNSTree sourceTree, RNSTree targetTree, TreePath targetPath, UIContext sourceContext,
		Collection<Pair<RNSTreeNode, RNSPath>> paths)
	{
		super(sourceContext, targetTree, targetPath, new RNSTreeOperatorSource(sourceTree, paths));
	}

	@Override
	public boolean performOperation()
	{
		if (_logger.isDebugEnabled())
			_logger.debug("RNSTreeMover called.");
		_uiContext.progressMonitorFactory()
			.createMonitor(_targetTree, "Moving Endpoints", "Moving endpoints.", 1000L, new MoverTask(), null).start();
		return true;
	}

	private class MoverTask extends AbstractTask<Integer>
	{
		@Override
		public Integer execute(TaskProgressListener progressListener) throws Exception
		{
			RNSTreeNode targetParentNode = (RNSTreeNode) _targetPath.getLastPathComponent();
			RNSFilledInTreeObject targetParentObject = (RNSFilledInTreeObject) targetParentNode.getUserObject();

			if (_sourceInformation.isRNSSource()) {
				RNSTreeOperatorSource source = (RNSTreeOperatorSource) _sourceInformation;

				for (Pair<RNSTreeNode, RNSPath> path : source.sourcePaths()) {
					progressListener.updateSubTitle(String.format("Moving %s", path.second().getName()));
					RNSPath target = getTargetPath(targetParentObject.path(), path.second().getName());
					if (target != null) {
						IContextResolver resolver = ContextManager.getResolver();

						try {
							ContextManager.setResolver(new MemoryBasedContextResolver(_uiContext.callingContext()));
							target.link(path.second().getEndpoint());
							new RefreshWorker(_targetTree, targetParentNode).run();
							path.second().unlink();
							new RefreshWorker(source.sourceTree(), path.first()).run();
						} catch (Throwable cause) {
							if (wasCancelled())
								return null;

							_logger.warn(String.format("Unable to move endpoint for source \"%s\".", path.second().pwd()),
								cause);
							ErrorHandler.handleError(_uiContext, _targetTree, cause);
						} finally {
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