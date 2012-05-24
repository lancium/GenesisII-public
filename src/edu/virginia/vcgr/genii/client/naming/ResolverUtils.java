package edu.virginia.vcgr.genii.client.naming;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.ogf.schemas.naming._2006._08.naming.ResolveFailedWithReferralFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.naming.EndpointIdentifierResolver;
import edu.virginia.vcgr.genii.naming.ReferenceResolver;
import edu.virginia.vcgr.genii.resolver.CountRequestType;
import edu.virginia.vcgr.genii.resolver.GeniiResolverPortType;
import edu.virginia.vcgr.genii.resolver.UpdateRequestType;
import edu.virginia.vcgr.genii.resolver.UpdateResponseType;

public class ResolverUtils {
	static private Log _logger = LogFactory.getLog(ResolverUtils.class);

	static public EndpointReferenceType resolve(EndpointReferenceType originalEPR)
		throws NameResolutionFailedException, ResourceException,
			GenesisIISecurityException, ConfigurationException, RemoteException
	{
		WSName wsname = new WSName(originalEPR);
		List<ResolverDescription> resolvers = wsname.getResolvers();
		for (ResolverDescription resolver : resolvers)
		{
			try
			{
				return resolve(resolver);
			}
			catch (Exception exception)
			{
				_logger.debug("ResolverUtils.resolve: " + exception);
			}
		}
		return null;
	}

	static public EndpointReferenceType resolve(ResolverDescription resolver)
		throws NameResolutionFailedException, ResourceException,
			GenesisIISecurityException, ConfigurationException, RemoteException
	{
		try
		{
			if (resolver.getType() == ResolverDescription.ResolverType.EPI_RESOLVER)
			{
				EndpointIdentifierResolver resolverPT = ClientUtils.createProxy(
						EndpointIdentifierResolver.class, resolver.getEPR());
				return resolverPT.resolveEPI(resolver.getEPI());
			}
			if (resolver.getType() == ResolverDescription.ResolverType.REFERENCE_RESOLVER)
			{
				ReferenceResolver resolverPT = ClientUtils.createProxy(
						ReferenceResolver.class, resolver.getEPR());
				return resolverPT.resolve(null);
			}
			throw new NameResolutionFailedException();
		}
		catch (ResolveFailedWithReferralFaultType rfe)
		{
			throw new NameResolutionFailedException();
		}
	}

	static public int getEndpointCount(EndpointReferenceType originalEPR)
	{
		WSName wsname = new WSName(originalEPR);
		URI targetEPI = wsname.getEndpointIdentifier();
		List<ResolverDescription> resolvers = wsname.getResolvers();
		for (ResolverDescription resolver : resolvers)
		{
			try
			{
				GeniiResolverPortType proxy = ClientUtils.createProxy(GeniiResolverPortType.class, resolver.getEPR());
				int[] targetIDList = proxy.getEndpointCount(new CountRequestType(targetEPI));
				return targetIDList.length;
			}
			catch (Exception exception)
			{
				_logger.debug("ResolverUtils.getEndpointCount: " + exception);
			}
		}
		return 0;
	}
	
	/**
	 * Register the given EPR with the given resolver resource.
	 * This returns an EPR with the address of the given EPR, and with a Resolver element.
	 */
	static public UpdateResponseType updateResolver(EndpointReferenceType resolverEPR, EndpointReferenceType entryReference)
		throws RemoteException
	{
		GeniiResolverPortType resolverService = ClientUtils.createProxy(
				GeniiResolverPortType.class, resolverEPR);
		return resolverService.update(new UpdateRequestType(entryReference));
	}

	/**
	 * Call primaryName.getResolvers().
	 * Also, deal with the special case where primaryName does not have a resolver element
	 * because it can resolve itself.
	 * 
	 * Perhaps all of the calls to wsname.getResolvers() throughout ResolverUtils should
	 * be changed to calls to getResolvers(wsname)?
	 */
	public static List<ResolverDescription> getResolvers(WSName primaryName)
	{
		List<ResolverDescription> resolverList = primaryName.getResolvers();
		if (resolverList.size() == 0)
		{
			// A resolver resource that is not registered with any resolvers can resolve itself.
			TypeInformation type = new TypeInformation(primaryName.getEndpoint());
			if (type.isEpiResolver())
			{
				URI epi = primaryName.getEndpointIdentifier();
				ResolverDescription rd = new ResolverDescription(epi, primaryName.getEndpoint(),
						ResolverDescription.ResolverType.EPI_RESOLVER);
				resolverList = new ArrayList<ResolverDescription>();
				resolverList.add(rd);
			}
		}
		return resolverList;
	}
}
