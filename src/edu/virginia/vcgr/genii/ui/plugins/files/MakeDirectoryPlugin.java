package edu.virginia.vcgr.genii.ui.plugins.files;

import java.io.Closeable;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

public class MakeDirectoryPlugin extends AbstractCombinedUIMenusPlugin
{
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		Closeable contextToken = null;

		while (true) {
			contextToken = null;

			try {
				contextToken = ContextManager.temporarilyAssumeContext(context.uiContext().callingContext());

				String answer = JOptionPane.showInputDialog(context.ownerComponent(),
					"What would you like to call the new directory?");
				if (answer == null)
					return;

				Collection<RNSPath> paths = context.endpointRetriever().getTargetEndpoints();
				RNSPath path = paths.iterator().next();
				path = path.lookup(answer, RNSPathQueryFlags.MUST_NOT_EXIST);
				path.mkdir();
				context.endpointRetriever().refresh();
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

		return selectedDescriptions.iterator().next().typeInformation().isRNS();
	}
}