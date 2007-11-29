package edu.virginia.vcgr.genii.container.informationService.resource;

import java.util.Collection;
import java.util.regex.Pattern;

import org.ggf.rns.EntryType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;


public interface IISResource extends IRNSResource{
	public void addResource(String resourceName, 
		EndpointReferenceType resourceEndpoint, ICallingContext callingContext) throws ResourceException;
	public Collection<EntryType> listResources(Pattern pattern) 
		throws ResourceException;
	public Collection<String> remove(Pattern pattern)
		throws ResourceException;
	public void configureResource(String resourceName, int numSlots)
		throws ResourceException;
	public Collection<String> removeEntries(String regex)
		throws ResourceException;
	public ICallingContext getContextInformation(String name) throws ResourceException;

}
