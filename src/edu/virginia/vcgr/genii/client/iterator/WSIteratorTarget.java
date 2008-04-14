package edu.virginia.vcgr.genii.client.iterator;

import java.rmi.RemoteException;

import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.ResourceNotDestroyedFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.iterator.IteratorPortType;

class WSIteratorTarget
{
	private int _referenceCount;
	private boolean _mustDestroy;
	private IteratorPortType _target;
	
	WSIteratorTarget(IteratorPortType target, boolean mustDestroy)
	{
		_referenceCount = 0;
		_mustDestroy = mustDestroy;
		_target = target;
	}
	
	WSIteratorTarget(EndpointReferenceType target, boolean mustDestroy)
		throws ConfigurationException, ResourceException, GenesisIISecurityException
	{
		this(ClientUtils.createProxy(
			IteratorPortType.class, target), mustDestroy);
	}
	
	synchronized IteratorPortType getTarget()
	{
		if (_referenceCount <= 0)
			throw new IllegalStateException(
				"Cannot get the ws-iterator target when the " +
				"reference count is not positive.");
		return _target;
	}
	
	synchronized void addReference()
	{
		_referenceCount++;
	}
	
	synchronized void releaseReference() 
		throws ResourceUnavailableFaultType, ResourceNotDestroyedFaultType, 
			ResourceUnknownFaultType, RemoteException
	{
		if (--_referenceCount <= 0 && _mustDestroy)
		{
			_target.destroy(null);
		}
	}
}