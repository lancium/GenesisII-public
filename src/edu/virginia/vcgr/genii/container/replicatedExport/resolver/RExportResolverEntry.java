package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import java.net.URI;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.replicatedExport.resolver.InvalidWSNameFaultType;

public class RExportResolverEntry 
{
	private URI _commonEPI = null;
	private EndpointReferenceType _primaryEPR = null;
	private EndpointReferenceType _replicaEPR = null;
	private URI _resolverEPI = null;
	private EndpointReferenceType _resolverEPR = null;
	private String _primaryLocalPath = null;
	private EndpointReferenceType _resolverServiceEPR = null;

	/**
	 * Creates new entry with all elements set by parameters.
	 * 
	 * @param commonEPI
	 * @param primaryEPR
	 * @param replicaEPR
	 * @param resolverEPI
	 * @param resolverEPR
	 * @param primaryLocalPath
	 * @param resolverServiceEPR
	 */
	public RExportResolverEntry(URI commonEPI, EndpointReferenceType primaryEPR, 
			EndpointReferenceType replicaEPR, URI resolverEPI, 
			EndpointReferenceType resolverEPR, String primaryLocalPath,
			EndpointReferenceType resolverServiceEPR)
	{
		_commonEPI = commonEPI;
		_primaryEPR = primaryEPR;
		_replicaEPR = replicaEPR;
		_resolverEPI = resolverEPI;
		_resolverEPR = resolverEPR;
		_primaryLocalPath = primaryLocalPath;
		_resolverServiceEPR = resolverServiceEPR;
	}
	
	/**
	 * Creates new entry with all but replicaEPR parameters set.
	 * commonEPI is extracted from passed in primaryEPR.
	 * 
	 * @param primaryEPR
	 * @param resolverEPI
	 * @param resolverEPR
	 * @param primaryLocalPath
	 * @param resolverServiceEPR
	 * @throws ResourceException
	 * @throws InvalidWSNameFaultType
	 */
	public RExportResolverEntry(EndpointReferenceType primaryEPR, URI resolverEPI, 
			EndpointReferenceType resolverEPR, String primaryLocalPath,
			EndpointReferenceType resolverServiceEPR)
		throws ResourceException, InvalidWSNameFaultType
	{
		WSName tmp = new WSName(primaryEPR);
		
		if (!tmp.isValidWSName())
			throw new InvalidWSNameFaultType();
		
		_commonEPI = tmp.getEndpointIdentifier();
		_primaryEPR = primaryEPR;		
		_resolverEPI = resolverEPI;
		_resolverEPR = resolverEPR;
		_primaryLocalPath = primaryLocalPath;
		_resolverServiceEPR = resolverServiceEPR;
	}
	
	public URI getCommonEPI()
	{
		return _commonEPI;
	}
	
	public void setCommonEPI(URI EPI)
	{
		_commonEPI = EPI;
	}
	
	public EndpointReferenceType getPrimaryEPR()
	{
		return _primaryEPR;
	}
	
	public void setPrimaryEPR(EndpointReferenceType targetEPR)
	{
		_primaryEPR = targetEPR;
	}
	
	public EndpointReferenceType getReplicaEPR()
	{
		return _replicaEPR;
	}
	
	public void setReplicaEPR(EndpointReferenceType replicaEPR)
	{
		_replicaEPR = replicaEPR;
	}
	
	public URI getResolverEPI()
	{
		return _resolverEPI;
	}
	
	public void setResolverEPI(URI resolverEPI)
	{
		_resolverEPI = resolverEPI;
	}
	
	public EndpointReferenceType getResolverEPR()
	{
		return _resolverEPR;
	}
	
	public void setResolverEPR(EndpointReferenceType resolverEPR)
	{
		_resolverEPR = resolverEPR;
	}
	
	public String getLocalPath(){
		return _primaryLocalPath;
	}
	
	public void setLocalPath(String path){
		_primaryLocalPath = path;
	}
	
	public EndpointReferenceType getResolverServiceEPR(){
		return _resolverServiceEPR;
	}
	
	public void setResolverServiceEPR(EndpointReferenceType resolverServiceEPR){
		_resolverServiceEPR = resolverServiceEPR;
	}
}