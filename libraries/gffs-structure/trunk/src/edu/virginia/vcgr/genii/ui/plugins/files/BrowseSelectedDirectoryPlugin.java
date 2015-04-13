package edu.virginia.vcgr.genii.ui.plugins.files;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.ClientApplication;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

public class BrowseSelectedDirectoryPlugin extends AbstractCombinedUIMenusPlugin
{
	static private Log _logger = LogFactory.getLog(BrowseSelectedDirectoryPlugin.class);

	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		if (context == null)
			return;
		if (_logger.isDebugEnabled())
			_logger.debug("Browse Selected Directory performMenuAction called.");

		try {
			Collection<RNSPath> paths = context.endpointRetriever().getTargetEndpoints();

			RNSPath path = paths.iterator().next();
			TypeInformation typeInfo = new TypeInformation(path.getEndpoint());
			if (!typeInfo.isRNS())
				throw new UIPluginException("Path \"" + path.pwd() + "\" is not an RNS directory.");
			UIContext newcontext = (UIContext) context.uiContext().clone();
			newcontext.setApplicationContext(new ApplicationContext());
			ClientApplication app = new ClientApplication(newcontext, false, path.pwd());
			app.pack();
			app.centerWindowAndMarch();
			app.setVisible(true);
		} catch (UIPluginException e) {
			// pass this one along.
			throw e;
		} catch (Throwable cause) {
			// we don't know what went wrong but log it.
			_logger.error("exception occurred while jumping to selected directory", cause);
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
		return (tp.isRNS());
	}
}