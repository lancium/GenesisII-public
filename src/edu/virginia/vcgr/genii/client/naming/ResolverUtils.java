package edu.virginia.vcgr.genii.client.naming;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.ogf.schemas.naming._2006._08.naming.ResolveFailedWithReferralFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.naming.EndpointIdentifierResolver;
import edu.virginia.vcgr.genii.naming.ReferenceResolver;

public class ResolverUtils {
	static private Log _logger = LogFactory.getLog(ResolverUtils.class);

	static public EndpointReferenceType resolve(
			EndpointReferenceType originalEPR)
			throws NameResolutionFailedException, ResourceException,
			GenesisIISecurityException, ConfigurationException, RemoteException {

		WSName wsName = new WSName(originalEPR);
		List<ResolverDescription> resolvers = 
			new ArrayList<ResolverDescription>(wsName.getResolvers());

		EndpointReferenceType retval = null;
		ListIterator<ResolverDescription> itr = resolvers.listIterator();
		while (itr.hasNext()) {
			ResolverDescription desc = itr.next();
			try {
				retval = resolve(desc);
			} catch (Exception e) {
				_logger.warn("EPR resolution failure", e);
				continue;
			}
			if (retval == null) {
				continue;
			} else if (EPRUtils.isUnboundEPR(retval)) {
				wsName = new WSName(retval);
				if (wsName.getResolvers() != null) {
					for (ResolverDescription newDesc : wsName.getResolvers()) {
						itr.add(newDesc);
					}
				}
			}

			break;
		}

		return retval;
	}

	static public EndpointReferenceType resolve(ResolverDescription resolver)
			throws NameResolutionFailedException, ResourceException,
			GenesisIISecurityException, ConfigurationException, RemoteException {
		try {
			if (resolver.getType() == ResolverDescription.ResolverType.EPI_RESOLVER) {
				EndpointIdentifierResolver resolverPT = ClientUtils
						.createProxy(EndpointIdentifierResolver.class, resolver
								.getEPR());
				return resolverPT.resolveEPI(new org.apache.axis.types.URI(
						resolver.getEPI().toString()));
			} else if (resolver.getType() == ResolverDescription.ResolverType.REFERENCE_RESOLVER) {
				ReferenceResolver resolverPT = ClientUtils.createProxy(
						ReferenceResolver.class, resolver.getEPR());
				return resolverPT.resolve(null);
			}
			throw new NameResolutionFailedException();
		} catch (org.apache.axis.types.URI.MalformedURIException mfe) {
			throw new NameResolutionFailedException(mfe);
		} catch (ResolveFailedWithReferralFaultType rfe) {
			throw new NameResolutionFailedException();
		}
	}

}
