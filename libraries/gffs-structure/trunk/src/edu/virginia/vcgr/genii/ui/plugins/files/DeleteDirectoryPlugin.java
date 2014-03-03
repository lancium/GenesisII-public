package edu.virginia.vcgr.genii.ui.plugins.files;

import java.io.Closeable;
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.tools.RmTool;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.PathOutcome;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;
import edu.virginia.vcgr.genii.ui.utils.LoggingTarget;

public class DeleteDirectoryPlugin extends AbstractCombinedUIMenusPlugin {
	static private Log _logger = LogFactory.getLog(DeleteDirectoryPlugin.class);
	JFileChooser _fileDialog = new JFileChooser();

	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType)
			throws UIPluginException {
		Closeable contextToken = null;
		if (context == null)
			return;
		if (_logger.isDebugEnabled())
			_logger.debug("DeleteDirectoryPlugin performMenuAction called.");
		while (true) {
			contextToken = null;
			try {
				contextToken = ContextManager.temporarilyAssumeContext(context
						.uiContext().callingContext());
				Collection<RNSPath> paths = context.endpointRetriever()
						.getTargetEndpoints();

				RNSPath path = paths.iterator().next();

				// We really need to figure out how to refresh directories after
				// deleting things -
				// ASG
				int reply = JOptionPane.showConfirmDialog(
						context.ownerComponent(),
						"Are you sure you want to recursively delete "
								+ path.getName() + "?",
						"Yes - DELETE WITHOUT UNDO " + path.getName(),
						JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION) {
					RmTool rmtool = new RmTool();
					PathOutcome ret;
					ret = rmtool.rm(path, true, false);
					if (PathOutcome.OUTCOME_SUCCESS.differs(ret)) {
						String msg = "failed to delete the chosen path: "
								+ path.getName() + " because "
								+ PathOutcome.outcomeText(ret);
						LoggingTarget.logInfo(msg, null);
						_logger.error(msg);
					}
					context.endpointRetriever().refreshParent();
					// path.delete();
				}
				return;
			} catch (Throwable cause) {
				ErrorHandler.handleError(context.uiContext(),
						context.ownerComponent(), cause);
			} finally {
				StreamUtils.close(contextToken);
			}
		}
	}

	@Override
	public boolean isEnabled(
			Collection<EndpointDescription> selectedDescriptions) {
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;

		// ASG: 9-13-2013. Modified to be more selective. Not just is it an RNS,
		// but is it an RNS
		// and NOT (isContainer, isBES ...
		// Perhaps should be even more selective,
		TypeInformation tp = selectedDescriptions.iterator().next()
				.typeInformation();
		return (tp.isRNS() && !(tp.isContainer() || tp.isBESContainer()
				|| tp.isQueue() || tp.isIDP()));
	}
}