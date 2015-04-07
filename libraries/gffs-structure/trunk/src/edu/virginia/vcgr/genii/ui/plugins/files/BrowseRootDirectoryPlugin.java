package edu.virginia.vcgr.genii.ui.plugins.files;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.gjt.gui.util.GUIUtils;
import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.ClientApplication;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

public class BrowseRootDirectoryPlugin extends AbstractCombinedUIMenusPlugin
{
	static private Log _logger = LogFactory.getLog(BrowseRootDirectoryPlugin.class);

	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		String TargetPath = "/";
		try {
			RNSPath path = RNSPath.getCurrent().lookup(TargetPath, RNSPathQueryFlags.MUST_EXIST);
			TypeInformation typeInfo = new TypeInformation(path.getEndpoint());
			if (!typeInfo.isRNS())
				throw new UIPluginException("Path \"" + path.pwd()
					+ "\" is not an RNS directory.  There is something very wrong with the grid connection, or the grid itself.");
			UIContext newcontext = (UIContext) context.uiContext().clone();
			newcontext.setApplicationContext(new ApplicationContext());
			ClientApplication app = new ClientApplication(newcontext, false, path.pwd());
			app.pack();
			GUIUtils.centerComponent(app);
			app.setVisible(true);
		} catch (UIPluginException e) {
			// pass this one along.
			throw e;
		} catch (Throwable cause) {
			// we don't know what went wrong but log it.
			_logger.error("exception occurred while jumping to root directory", cause);
		}
	}

	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		return true;
	}
}