package edu.virginia.vcgr.genii.ui.plugins.fsproxy;

import java.awt.Dialog.ModalityType;
import java.io.Closeable;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.morgan.util.io.StreamUtils;
import org.morgan.utils.gui.GUIUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.g3.fsview.FSViewConnectionInformation;
import edu.virginia.g3.fsview.gui.FSViewCreatorDialog;
import edu.virginia.vcgr.genii.client.cmd.tools.CreateResourceTool;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.exportdir.FSProxyConstructionParameters;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.ui.EndpointType;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;
import edu.virginia.vcgr.genii.ui.rns.RNSFilledInTreeObject;
import edu.virginia.vcgr.genii.ui.rns.RNSSelectionDialog;
import edu.virginia.vcgr.genii.ui.rns.RNSSelectionFilter;

public class CreateFSProxyPlugin extends AbstractCombinedUIMenusPlugin
{
	private class RNSTreeSelectionContainerFilter implements RNSSelectionFilter
	{
		@Override
		public boolean accept(RNSPath path, EndpointReferenceType epr,
			TypeInformation typeInformation, EndpointType displayType,
			boolean isLocal)
		{
			return typeInformation.isContainer();
		}
	}
	
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType)
			throws UIPluginException
	{
		Closeable contextToken = null;
		
		while (true)
		{
			contextToken = null;
			
			try
			{
				contextToken = ContextManager.temporarilyAssumeContext(
					context.uiContext().callingContext());
				
				FSViewCreatorDialog dialog = new FSViewCreatorDialog(
					SwingUtilities.getWindowAncestor(context.ownerComponent()));
				dialog.pack();
				dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
				GUIUtils.centerWindow(dialog);
				dialog.setVisible(true);
				FSViewConnectionInformation connectInfo = 
					dialog.connectionInformation();
				
				if (connectInfo != null)
				{
					String answer = JOptionPane.showInputDialog(context.ownerComponent(),
						"What would you like to call the filesystem proxy mount?");
					if (answer == null)
						return;
					
					Collection<RNSPath> paths = 
						context.endpointRetriever().getTargetEndpoints();
					RNSPath path = paths.iterator().next();
					path = path.lookup(answer, RNSPathQueryFlags.MUST_NOT_EXIST);
					
					RNSSelectionDialog rDialog = new RNSSelectionDialog(
						SwingUtilities.getWindowAncestor(context.ownerComponent()),
						context.uiContext(), new RNSTreeSelectionContainerFilter());
					rDialog.pack();
					rDialog.setModalityType(ModalityType.DOCUMENT_MODAL);
					GUIUtils.centerWindow(rDialog);
					rDialog.setVisible(true);
					
					RNSFilledInTreeObject fObj = rDialog.selectedRNSPath();
					if (fObj == null)
						return;
					
					RNSPath proxyService = fObj.path().lookup("Services/FSProxyPortType");
					CreateResourceTool.createInstance(
						proxyService.getEndpoint(), new GeniiPath(path.pwd()),
						new FSProxyConstructionParameters(connectInfo), connectInfo.shortName());
					
					context.endpointRetriever().refresh();
				}
				
				return;
			}
			catch (Throwable cause)
			{
				ErrorHandler.handleError(context.uiContext(),
					context.ownerComponent(), cause);
			}
			finally
			{
				StreamUtils.close(contextToken);
			}
		}
	}

	@Override
	public boolean isEnabled(
		Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;
		
		return selectedDescriptions.iterator().next().typeInformation().isRNS();
	}
}