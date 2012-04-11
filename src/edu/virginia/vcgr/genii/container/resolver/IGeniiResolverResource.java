package edu.virginia.vcgr.genii.container.resolver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.apache.axis.types.URI;
import org.ws.addressing.EndpointReferenceType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface IGeniiResolverResource extends IResource
{
	public void addTargetEPR(URI targetEPI, int targetID, EndpointReferenceType targetEPR)
		throws ResourceException;
	public void removeTargetEPR(URI targetEPI, int targetID)
		throws ResourceException;
	public URI[] getTargetEPIList()
		throws ResourceException;
	public EndpointReferenceType getTargetEPR(URI targetEPI, int targetID)
		throws ResourceException;
	public int[] getTargetIDList(URI targetEPI)
		throws ResourceException;
	public int getEntryCount()
		throws ResourceException;
	public HashMap<URI, String> listAllResolvers() 
		throws ResourceException;
	public void writeAllEntries(ObjectOutputStream ostream)
		throws ResourceException, IOException;
}
