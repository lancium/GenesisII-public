package edu.virginia.vcgr.genii.container.genesis_dai.resource;

import java.util.Collection;

import org.ggf.rns.EntryType;
import org.ws.addressing.EndpointReferenceType;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;

public interface IDAIResource extends IRNSResource{

	public void addEntry( String serviceName, EndpointReferenceType serviceEndpoint,
			String resourceName, EndpointReferenceType resourceEndpoint) 
		throws ResourceException;
	
	public Collection<String> removeEntries(String serviceName)
		throws ResourceException;
	
	public Collection<EntryType> listResources(Pattern pattern) 
		throws ResourceException;
	
	public Collection<String> remove(Pattern pattern) 
		throws ResourceException;
	
	public void configureResource(String resourceName, int numSlots)
		throws ResourceException;
}
