package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.resource.IResource;

interface IRExportResolverResource extends IResource
{
	public void update(RExportResolverEntry entry) throws ResourceException, ResourceUnknownFaultType;

	public RExportResolverEntry getEntry() throws ResourceException;

	public void updateResolverResourceInfo(String resourceEPI, String resolverEPI, EndpointReferenceType resolverEPR,
		boolean isResolverTermination) throws ResourceException;

	public EndpointReferenceType queryForResourceResolver(String resourceEPI) throws ResourceException;

}