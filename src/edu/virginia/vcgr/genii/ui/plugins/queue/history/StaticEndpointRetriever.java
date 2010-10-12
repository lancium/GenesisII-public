package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.util.ArrayList;
import java.util.Collection;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.plugins.EndpointRetriever;

class StaticEndpointRetriever implements EndpointRetriever
{
	private EndpointReferenceType _endpoint;
	
	StaticEndpointRetriever(EndpointReferenceType endpoint)
	{
		_endpoint = endpoint;
	}
	
	@Override
	final public Collection<RNSPath> getTargetEndpoints()
	{
		ArrayList<RNSPath> ret = new ArrayList<RNSPath>(1);
		ret.add(new RNSPath(_endpoint));
		
		return ret;
	}

	@Override
	final public void refresh()
	{
		// Do nothing
	}

	@Override
	final public void refreshParent()
	{
		// Do nothing
	}
}