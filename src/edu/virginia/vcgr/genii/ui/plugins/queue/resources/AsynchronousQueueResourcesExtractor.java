package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import java.util.concurrent.Callable;

import org.ggf.rns.List;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rfork.ResourceForkUtils;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;

class AsynchronousQueueResourcesExtractor
	implements Callable<EnhancedRNSPortType>
{
	private UIPluginContext _uiContext;
	
	AsynchronousQueueResourcesExtractor(
		UIPluginContext uiContext)
	{
		_uiContext = uiContext;
	}
	
	@Override
	final public EnhancedRNSPortType call() throws Exception
	{
		EndpointReferenceType queueEPR =
			_uiContext.endpointRetriever().getTargetEndpoints().iterator().next().getEndpoint();
		
		queueEPR = ResourceForkUtils.stripResourceForkInformation(queueEPR);
		EnhancedRNSPortType rpt = ClientUtils.createProxy(
			EnhancedRNSPortType.class, queueEPR,
			_uiContext.uiContext().callingContext());
		EndpointReferenceType resourcesEPR = rpt.list(
			new List("resources")).getEntryList()[0].getEntry_reference();
		return ClientUtils.createProxy(EnhancedRNSPortType.class,
			resourcesEPR, _uiContext.uiContext().callingContext());
	}
}