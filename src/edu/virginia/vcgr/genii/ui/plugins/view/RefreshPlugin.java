package edu.virginia.vcgr.genii.ui.plugins.view;

import java.io.Closeable;
import java.util.Collection;
import org.morgan.util.io.StreamUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

/**
 * Implements a view menu item for refreshing the RNS tree.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class RefreshPlugin extends AbstractCombinedUIMenusPlugin
{
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		Closeable contextToken = null;

		while (true) {
			contextToken = null;
			try {
				contextToken = ContextManager.temporarilyAssumeContext(context.uiContext().callingContext());
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
