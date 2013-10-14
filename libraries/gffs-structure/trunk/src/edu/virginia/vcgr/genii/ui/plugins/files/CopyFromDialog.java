package edu.virginia.vcgr.genii.ui.plugins.files;

import java.io.Closeable;
import java.util.Collection;

import javax.swing.JFileChooser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.CopyMachine;
import edu.virginia.vcgr.genii.client.rns.PathOutcome;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;
import edu.virginia.vcgr.genii.ui.utils.LoggingTarget;

/**
 * Provides a dialog for copying a local directory system to RNS path
 * 
 * @author Andrew Grimshaw based on code by Chris Koeritz
 * @copyright Copyright (c) 2013 By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class CopyFromDialog extends AbstractCombinedUIMenusPlugin
{
	static private Log _logger = LogFactory.getLog(CopyFromDialog.class);
	JFileChooser _fileDialog = new JFileChooser();

	/**
	 * We support both RNS directories and ByteIO files with this plugin.
	 */
	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;
		// ASG: 9-13-2013. Modified to be more selective. Not just is it an RNS, but is it an RNS and NOT (isContainer, isBES ...
		// Perhaps should be even more selective, 
		TypeInformation tp = selectedDescriptions.iterator().next().typeInformation();
		return ((tp.isRNS()|| tp.isByteIO() ) && !(tp.isContainer() || tp.isBESContainer() || tp.isQueue() || tp.isIDP()));
	}

	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		if (context == null)
			return;
		if (_logger.isDebugEnabled())
			_logger.debug("CopyFromDialog performMenuAction called.");
		Closeable contextToken = null;
		contextToken = null;
		try {
			contextToken = ContextManager.temporarilyAssumeContext(context.uiContext().callingContext());

			_fileDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int retPick = _fileDialog.showOpenDialog(context.ownerComponent());
			if (retPick == JFileChooser.APPROVE_OPTION) {
				Collection<RNSPath> paths = context.endpointRetriever().getTargetEndpoints();
				RNSPath path = paths.iterator().next();
				context
					.uiContext()
					.progressMonitorFactory()
					.createMonitor(context.ownerComponent(), "Copy from local filesystem", "", 1000L,
						new SaveToTask("local:" +_fileDialog.getSelectedFile().toString(), path.pwd()), null).start();
			}
		} catch (Throwable cause) {
			ErrorHandler.handleError(context.uiContext(), context.ownerComponent(), cause);
		} finally {
			StreamUtils.close(contextToken);
		}
	}

	private class SaveToTask extends AbstractTask<Integer>
	{
		String src;
		String target;

		SaveToTask(String pathIn, String targetIn)
		{
			src = pathIn;
			target = targetIn;
		}

		private PathOutcome performSave(TaskProgressListener progressListener)
		{
			if ((src == null) || (progressListener == null))
				return null;
			// we assume they don't want to overwrite files without knowing it.
			CopyMachine cm = new CopyMachine(src, target, progressListener, false, null, null);
			return cm.copyTree();
		}

		@Override
		public Integer execute(TaskProgressListener progressListener) throws Exception
		{
			if (progressListener == null)
				return 1;
			PathOutcome ret = performSave( progressListener);
			if (PathOutcome.OUTCOME_SUCCESS.differs(ret)) {
				String msg = "failed to save to the chosen path: " + target + " because " + PathOutcome.outcomeText(ret);
				LoggingTarget.logInfo(msg, null);
				_logger.error(msg);
				return 1;
			}
			return 0;
		}

		@Override
		public boolean showProgressDialog()
		{
			return true;
		}

	}
}
