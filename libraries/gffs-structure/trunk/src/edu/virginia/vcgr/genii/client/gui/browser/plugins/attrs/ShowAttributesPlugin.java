package edu.virginia.vcgr.genii.client.gui.browser.plugins.attrs;

import java.awt.Component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.gui.browser.plugins.ITabPlugin;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginStatus;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

/**
 * The ShowAttributesPlugin is a simple plugin which, in a tab, displays the attributes document for
 * a selected resource.
 * 
 * @author mmm2a
 */
public class ShowAttributesPlugin implements ITabPlugin
{
	static private Log _logger = LogFactory.getLog(ShowAttributesPlugin.class);

	@Override
	public Component getComponent(RNSPath[] selectedPaths) throws PluginException
	{
		/*
		 * All we do to show the attributes is create a new panel that has the attributes contained
		 * within.
		 */
		return new ShowAttrsPanel(selectedPaths[0]);
	}

	@Override
	public PluginStatus getStatus(RNSPath[] selectedResources) throws PluginException
	{
		/*
		 * If the currently selected paths exists and contains exactly one path endpoint, then we
		 * can show the attributes.
		 */
		if (selectedResources != null && selectedResources.length > 0 && selectedResources[0].exists()) {
			try {
				TypeInformation typeInfo = new TypeInformation(selectedResources[0].getEndpoint());

				/*
				 * In order to show attributes, we also require that the endpoint supports atleast
				 * one known interface. This is essentially equivalent to saying that the endpoint
				 * is a Genesis II endpoint (i.e., supports attributes).
				 */
				if (!typeInfo.isUnknown())
					return PluginStatus.ACTIVTE;
			} catch (RNSPathDoesNotExistException e) {
				/*
				 * This really shouldn't happen as we already asked if it exists.
				 */
				_logger.error("Unexpected exception.", e);
			}
		}

		return PluginStatus.HIDDEN;
	}
}