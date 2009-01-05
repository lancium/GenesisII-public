package edu.virginia.vcgr.genii.container.rfork;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;

public abstract class AbstractRNSResourceFork extends AbstractResourceFork
	implements RNSResourceFork
{
	protected AbstractRNSResourceFork(ResourceForkService service, 
		String forkPath)
	{
		super(service, forkPath);
	}
	
	protected InternalEntry createInternalEntry(
		EndpointReferenceType exemplarEPR, String entryName,
		ResourceForkInformation rif) 
			throws ResourceUnknownFaultType, ResourceException
	{
		return new InternalEntry(entryName,
			getService().createForkEPR(formForkPath(entryName), rif),
			null);
	}
	
	protected String formForkPath(String entryName)
	{
		String path = getForkPath();
		if (path.endsWith("/"))
			return path + entryName;
		return path + "/" + entryName;
	}
}