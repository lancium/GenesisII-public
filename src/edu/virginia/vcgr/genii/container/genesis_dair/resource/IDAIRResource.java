package edu.virginia.vcgr.genii.container.genesis_dair.resource;

import java.util.Collection;
import java.util.regex.Pattern;

import org.ggf.rns.EntryType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;

public interface IDAIRResource extends IRNSResource{
	
	public void addEntry( EndpointReferenceType serviceEPR, String resourceName, 
			EndpointReferenceType resourceEPR) 
		throws ResourceException;
	
	public Collection<String> removeEntries(String name) 
		throws ResourceException;
	
	public Collection<EntryType> listResources(Pattern pattern) 
		throws ResourceException;
	
	public Collection<String> remove(Pattern pattern) 
		throws ResourceException;
	
	public void configureResource(String resourceName, int numSlots) 
		throws ResourceException;
}
