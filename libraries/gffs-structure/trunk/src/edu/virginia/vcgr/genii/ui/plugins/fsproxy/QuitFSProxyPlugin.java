package edu.virginia.vcgr.genii.ui.plugins.fsproxy;

import java.io.Closeable;
import java.util.Collection;

import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.tools.ExportTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

public class QuitFSProxyPlugin extends AbstractCombinedUIMenusPlugin
{
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		Collection<RNSPath> targets = context.endpointRetriever().getTargetEndpoints();
		Closeable token = null;

		try {
			token = ContextManager.temporarilyAssumeContext(context.uiContext().callingContext());

			for (RNSPath target : targets) {
				try {
					EndpointReferenceType targetAddress = target.getEndpoint();
					TypeInformation typeInfo = new TypeInformation(targetAddress);
					if (typeInfo.isExport() || typeInfo.isLightweightExport())
						ExportTool.quitExportedRoot(targetAddress, false);
					else {
						GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, targetAddress);
						common.destroy(new Destroy());
					}

					target.unlink();
				} catch (Throwable cause) {
					ErrorHandler.handleError(context.uiContext(), context.ownerComponent(), cause);
				}
			}

			context.endpointRetriever().refreshParent();
		} finally {
			StreamUtils.close(token);
		}
	}

	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() == 0)
			return false;

		for (EndpointDescription ed : selectedDescriptions) {
			TypeInformation typeInfo = ed.typeInformation();
			if (!(typeInfo.isExport() || typeInfo.isLightweightExport() || typeInfo.isFSProxy()))
				return false;
		}

		return true;
	}
}