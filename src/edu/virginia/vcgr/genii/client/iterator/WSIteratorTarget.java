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
import edu.virginia.vcgr.genii.iterator.IteratorInitializationType;
import edu.virginia.vcgr.genii.iterator.IteratorMemberType;
import edu.virginia.vcgr.genii.iterator.IteratorPortType;

class WSIteratorTarget
{
	private int _referenceCount;
	private boolean _mustDestroy;
	private IteratorMemberType []_initialValues;
	private IteratorPortType _target;
	
	WSIteratorTarget(IteratorPortType target, 
		IteratorMemberType []initValues, boolean mustDestroy)
	{
		_referenceCount = 0;
		_mustDestroy = mustDestroy;
		_target = target;
		_initialValues = initValues;
	}
	
	WSIteratorTarget(IteratorInitializationType iterator, boolean mustDestroy)
		throws ConfigurationException, ResourceException, 
			GenesisIISecurityException
	{
		this(iterator.getIteratorEndpoint() == null ? null : ClientUtils.createProxy(
			IteratorPortType.class, iterator.getIteratorEndpoint()), 
			iterator.getBatchElement(), mustDestroy);
	}
	
	WSIteratorTarget(IteratorPortType target, boolean mustDestroy)
	{
		this(target, null, mustDestroy);
	}
	
	WSIteratorTarget(EndpointReferenceType target, boolean mustDestroy)
		throws ConfigurationException, ResourceException, GenesisIISecurityException
	{
		this(target == null ? null : ClientUtils.createProxy(
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
	
	synchronized IteratorMemberType[] getInitialValues()
	{
		if (_referenceCount <= 0)
			throw new IllegalStateException(
				"Cannot get ws-iterator initial values when the " +
				"reference count is not positive.");
		return _initialValues;
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