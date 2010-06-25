package edu.virginia.vcgr.genii.ui.plugins.matchparam;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyRefresher;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.MatchingParameter;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;

public class MatchingParametersPlugin extends AbstractCombinedUIMenusPlugin
{
	static private Log _logger = LogFactory.getLog(MatchingParametersPlugin.class);
	
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType)
			throws UIPluginException
	{
		Collection<Pair<String, String>> parameters =
			new LinkedList<Pair<String,String>>();
		Collection<Pair<Pair<String, String>, MatchingParameterOperation>> ops;
		
		try
		{
			EndpointReferenceType target = 
				context.endpointRetriever().getTargetEndpoints().iterator(
					).next().getEndpoint();
			GenesisIIBaseRP rp = 
				(GenesisIIBaseRP)ResourcePropertyManager.createRPInterface(
						context.uiContext().callingContext(),
						target, GenesisIIBaseRP.class);
			for (MatchingParameter mp : rp.getMatchingParameter())
			{
				parameters.add(new Pair<String, String>(
					mp.getName(), mp.getValue()));
			}
			
			ops = MatchingParameterDialog.handleMatchingParameters(
				context.ownerComponent(), parameters);
			if (ops != null && ops.size() > 0)
			{
				GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class,
					target, context.uiContext().callingContext());
				Vector<MatchingParameter> adds =
					new Vector<MatchingParameter>();
				Vector<MatchingParameter> deletes =
					new Vector<MatchingParameter>();
				
				for (Pair<Pair<String, String>, MatchingParameterOperation> op : ops)
				{
					Pair<String, String> parameter = op.first();
					MatchingParameterOperation operation = op.second();
					
					_logger.debug(operation.toString(parameter));
					if (operation == MatchingParameterOperation.Add)
						adds.add(new MatchingParameter(
							parameter.first(), parameter.second()));
					else
						deletes.add(new MatchingParameter(
							parameter.first(), parameter.second()));
				}
				
				if (adds.size() > 0)
					common.addMatchingParameter(adds.toArray(
						new MatchingParameter[adds.size()]));
				if (deletes.size() > 0)
					common.removeMatchingParameter(deletes.toArray(
						new MatchingParameter[deletes.size()]));
				((ResourcePropertyRefresher)rp).refreshResourceProperties();
				
				JOptionPane.showMessageDialog(context.ownerComponent(),
					"Please note that it may take a few minutes for the changes to show up.",
					"Matching Parameters Notice", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		catch (Throwable cause)
		{
			ErrorHandler.handleError(context.uiContext(),
				context.ownerComponent(), cause);
		}
		finally
		{
		}
	}

	@Override
	public boolean isEnabled(
		Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;
		
		TypeInformation typeInfo = 
			selectedDescriptions.iterator().next().typeInformation();
		
		return typeInfo.getGenesisIIContainerID() != null;
	}
}