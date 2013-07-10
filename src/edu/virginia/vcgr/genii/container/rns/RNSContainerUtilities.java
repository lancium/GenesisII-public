package edu.virginia.vcgr.genii.container.rns;

import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.RNSEntryResponseType;

import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorWrapper;
import edu.virginia.vcgr.genii.container.iterator.IteratorBuilder;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.security.authz.providers.AclAuthZProvider;
import edu.virginia.vcgr.genii.iterator.IterableElementType;
import edu.virginia.vcgr.genii.iterator.IteratorInitializationType;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.acl.Acl;
import edu.virginia.vcgr.genii.security.acl.AclEntry;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;

public class RNSContainerUtilities
{
	static private Log _logger = LogFactory.getLog(RNSContainerUtilities.class);

	static public LookupResponseType translate(Iterable<RNSEntryResponseType> entries, IteratorBuilder<Object> builder)
		throws RemoteException
	{

		return indexedTranslate(entries, builder, null);
	}

	public static LookupResponseType indexedTranslate(Iterable<RNSEntryResponseType> entries, IteratorBuilder<Object> builder,
		InMemoryIteratorWrapper imiw) throws RemoteException
 {

		builder.preferredBatchSize(RNSConstants.PREFERRED_BATCH_SIZE);
		builder.addElements(entries);
		// Next create the iterator as before
		IteratorInitializationType iit = builder.create(imiw);
		// Changed by ASG to have RNS iterators have the same access control as
		// the directory from which they are created. 7/3/2013
		// First, get the ACL of the directory itself
		// resource=null;
		if (iit.getIteratorEndpoint() != null) {
			Acl acl = null;
			ResourceKey rKey = ResourceManager.getCurrentResource();
			if (rKey != null) {
				try {
					// Get the access control list of the directory
					acl = (Acl) rKey.dereference().getProperty(
							AclAuthZProvider.GENII_ACL_PROPERTY_NAME);
					// Now add "w" everyone -- so the client can clean up
					// (destroy) the iterator properly later
					acl.writeAcl.add(null);
				} catch (ResourceException e1) {
					_logger.warn("failed to look up the ACL for resource "
							+ rKey.dereference());
				}
				// Now that the iterator is created and the ACL built up, set
				// the ACL of the iterator to that of the directory.
				ResourceManager
						.getTargetResource(iit.getIteratorEndpoint())
						.dereference()
						.setProperty(AclAuthZProvider.GENII_ACL_PROPERTY_NAME,
								acl);
			}
		}

		Collection<RNSEntryResponseType> batch = null;
		IterableElementType[] iet = iit.getBatchElement();
		if (iet != null && iet.length > 0) {
			batch = new ArrayList<RNSEntryResponseType>(iet.length);
			int lcv = 0;
			for (RNSEntryResponseType t : entries) {
				if (lcv >= iet.length)
					break;
				batch.add(t);
				lcv++;
			}
		}

		return new LookupResponseType(batch == null ? null
				: batch.toArray(new RNSEntryResponseType[batch.size()]),
				iit.getIteratorEndpoint());
	}

	public static NuCredential loadRNSResourceCredential(IRNSResource resource)
	{
		NuCredential credential = null;
		try {
			credential = (NuCredential) resource.getProperty(SecurityConstants.IDP_STORED_CREDENTIAL_QNAME.getLocalPart());
		} catch (ResourceException e) {
			_logger.error("resource exception loading credential, quashing");
		}

		if (credential == null) {
			_logger.warn("found null credential for resource: " + resource.toString() + "  is this db conversion issue?");
			X509Certificate[] resourceCertChain = null;
			try {
				resourceCertChain = (X509Certificate[]) resource.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
			} catch (ResourceException e) {
				_logger.error("failed to load resource certificate chain!  this is quite bad.  resource is: "
					+ resource.toString());
			}

			// trying to find the real types involved by looking at ACLs.
			Acl acl = null;
			try {
				acl = (Acl) resource.getProperty(AclAuthZProvider.GENII_ACL_PROPERTY_NAME);
			} catch (ResourceException e1) {
				_logger.warn("failed to look up the ACL for resource " + resource);
			}

			// we will fill this in if we can find it in the resource ACL.
			X509Identity found = null;
			if (acl != null) {
				Collection<AclEntry> trustList = acl.readAcl;
				for (AclEntry entry : trustList) {
					if (entry instanceof X509Identity) {
						X509Identity id = (X509Identity) entry;
						if (id.getOriginalAsserter()[0].equals(resourceCertChain[0])) {
							found = id;
						}
					}
				}
			}
			if (found != null) {
				if (_logger.isDebugEnabled())
					_logger.debug("found an identity that matches our certificate: " + found);
				credential = found;
			} else {
				if (_logger.isDebugEnabled())
					_logger.debug("found no identity matching our certificate, so using type of OTHER.");
				credential = new X509Identity(resourceCertChain, IdentityType.OTHER);
			}
			// store the new credential back for the resource.
			try {
				resource.setProperty(SecurityConstants.IDP_STORED_CREDENTIAL_QNAME.getLocalPart(), credential);
			} catch (ResourceException e) {
				_logger.error("failed to save credential for: " + resource.toString());
			}
		}

		return credential;
	}
}
