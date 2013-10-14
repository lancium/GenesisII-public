package edu.virginia.vcgr.genii.ui.plugins.files;

import java.io.Closeable;
import java.util.Collection;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

public class DeleteFilePlugin extends AbstractCombinedUIMenusPlugin
{
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		Closeable contextToken = null;

		while (true) {
			contextToken = null;

			try {
				contextToken = ContextManager.temporarilyAssumeContext(context.uiContext().callingContext());

				Collection<RNSPath> paths = context.endpointRetriever().getTargetEndpoints();
				RNSPath path = paths.iterator().next();
				// We really need to figure out how to refresh directories after deleting things -
				// ASG
				path.delete();

				return;
			} catch (Throwable cause) {
				ErrorHandler.handleError(context.uiContext(), context.ownerComponent(), cause);
			} finally {
				StreamUtils.close(contextToken);
			}
		}
	}

	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;

		// ASG: 9-13-2013. Modified to be more selective. Not just is it an RNS, but is it an RNS
		// and NOT (isContainer, isBES ...
		// Perhaps should be even more selective,
		TypeInformation tp = selectedDescriptions.iterator().next().typeInformation();
		return (tp.isByteIO() && !(tp.isContainer() || tp.isBESContainer() || tp.isQueue() || tp.isIDP()));
	}
}