package edu.virginia.vcgr.genii.client.rns;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.rns.RNSMetadataType;
import org.ggf.rns.ReadNotPermittedFaultType;
import org.ggf.rns.WriteNotPermittedFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileRequestType;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;

final public class RNSLegacyProxy
{
	private EnhancedRNSPortType _newClient;
	private ICallingContext _callContext;
	
	public RNSLegacyProxy(EnhancedRNSPortType newClient, 
		ICallingContext callContext)
	{
		try
		{
			_newClient = newClient;
			_callContext = callContext == null ? 
				ContextManager.getCurrentContext() : callContext;
		}
		catch (IOException ioe)
		{
			throw new IllegalArgumentException(
				"Calling context invalid!", ioe);
		}
	}
	
	public RNSLegacyProxy(EnhancedRNSPortType newClient)
	{
		this(newClient, null);
	}
	
	final public RNSEntryResponseType[] lookup(String...names)
		throws ReadNotPermittedFaultType, RemoteException
	{
		return iterateList(names).toArray();
	}
	
	final public RNSIterable iterateList(String...names)
		throws ReadNotPermittedFaultType, RemoteException
	{
		LookupResponseType resp;
		
		if (names == null || names.length == 0)
			resp = _newClient.lookup(null);
		else
			resp = _newClient.lookup(names);
		
		return new RNSIterable(resp, _callContext, RNSConstants.PREFERRED_BATCH_SIZE);
	}
	
	final public EndpointReferenceType add(
		String name, EndpointReferenceType epr, MessageElement []any) 
			throws WriteNotPermittedFaultType, RemoteException
	{
		RNSMetadataType mdt = null;
		if (any != null && any.length > 0)
			mdt = RNSUtilities.createMetadata(epr, any);
		
		RNSEntryResponseType rpt = _newClient.add(new RNSEntryType[] { 
			new RNSEntryType(epr, mdt, name) })[0];
		BaseFaultType fault = rpt.getFault();
		if (fault != null)
			throw new RemoteException(String.format(
				"Unable to add %s!", name), fault);
		
		return rpt.getEndpoint();
	}
	
	final public EndpointReferenceType add(String name, EndpointReferenceType epr) 
		throws WriteNotPermittedFaultType, RemoteException
	{
		return add(name, epr, null);
	}
	
	final public EndpointReferenceType add(String name)
		throws WriteNotPermittedFaultType, RemoteException
	{
		return add(name, null);
	}
	
	final public EndpointReferenceType createRoot()
		throws WriteNotPermittedFaultType, RemoteException
	{
		return add(null);
	}
	
	final public Set<String> remove(String...names)
		throws WriteNotPermittedFaultType, RemoteException
	{
		RNSEntryResponseType []rpt;
		
		if (names == null || names.length == 0)
			rpt = _newClient.remove(null);
		else
			rpt = _newClient.remove(names);
		
		Set<String> ret = new HashSet<String>();
		for (RNSEntryResponseType resp : rpt)
		{
			if (resp.getFault() != null)
				throw new RemoteException(String.format(
					"Unable to remove entry %s!", resp.getEntryName()), 
					resp.getFault());
			
			ret.add(resp.getEntryName());
		}
		
		return ret;
	}
	
	final public EndpointReferenceType createFile(String fileName) 
		throws RemoteException
	{
		return _newClient.createFile(
			new CreateFileRequestType(fileName)).getEndpoint();
	}
}