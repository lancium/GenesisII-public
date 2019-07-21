package edu.virginia.vcgr.genii.container.q2;

import java.io.IOException;
import java.util.Collection;

import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.KeystoreManager;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.axis.AxisAcl;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.security.authz.providers.AuthZProviders;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;
import edu.virginia.vcgr.genii.security.acl.Acl;
import edu.virginia.vcgr.genii.security.acl.AclEntry;
import edu.virginia.vcgr.genii.security.identity.Identity;
import edu.virginia.vcgr.genii.security.utils.SecurityUtilities;

/**
 * This class is a collection of utilities that the queue uses to manipulate certain aspects of security implemented by the queue. Mostly
 * having to do with the association of identities with jobs (ownership relationship).
 * 
 * @author mmm2a
 */
public class QueueSecurity
{
	/**
	 * Retrieve the list of identities associated with the current calling context. The queue will associate these identities with being the
	 * "caller's" identities.
	 * 
	 * @return The list of identities.
	 * 
	 * @throws AuthZSecurityException
	 */
	static public Collection<Identity> getCallerIdentities(boolean filterOutGroups) throws AuthZSecurityException
	{
		try {
			/* Retrieve the current calling context */
			ICallingContext callingContext = ContextManager.getExistingContext();

			if (filterOutGroups)
				return SecurityUtilities.filterCredentials(KeystoreManager.getCallerIdentities(callingContext),
					SecurityUtilities.GROUP_TOKEN_PATTERN);
			else
				return KeystoreManager.getCallerIdentities(callingContext);
		} catch (AuthZSecurityException gse) {
			throw new AuthZSecurityException("Unable to load current context.", gse);
		} catch (IOException ioe) {
			throw new AuthZSecurityException("Unable to load current context.", ioe);
		}
	}

	/**
	 * Check to see if the current caller has any matches in the list of supplied job owners. We have a match if any of the identities in the
	 * current calling context matches any of the identities in the listed job owners.
	 * 
	 * @param jobOwners
	 *            The list of job owners that we are checking against.
	 * 
	 * @return True if the calling context represents an owner of the indicated job owners, false otherwise.
	 * 
	 * @throws AuthZSecurityException
	 */
	static public boolean isOwner(Collection<Identity> jobOwners) throws AuthZSecurityException
	{
		if (isQueueAdmin())
			return true;

		/* If the job has no owners, then we automatically match */
		if (jobOwners == null || jobOwners.size() == 0)
			return true;

		/* Now, get the caller's identities. */
		Collection<Identity> callers = QueueSecurity.getCallerIdentities(true);

		/* For each identity that owns the job... */
		for (Identity jobOwner : jobOwners) {
			/* Check to see if the caller has a matching identity. */
			if (isOwner(callers, jobOwner))
				return true;
		}

		return false;
	}

	static public boolean isOwner(Collection<Identity> jobOwners, Collection<Identity> callers) throws AuthZSecurityException
	{

		/* If the job has no owners, then we automatically match */
		if (jobOwners == null || jobOwners.size() == 0)
			return true;

		/* For each identity that owns the job... */
		for (Identity jobOwner : jobOwners) {
			/* Check to see if the caller has a matching identity. */
			if (isOwner(callers, jobOwner))
				return true;
		}

		return false;
	}

	/**
	 * Check to see if any of the caller's identities match the identity of the job given.
	 * 
	 * @param callers
	 *            The list of identities the caller has.
	 * @param jobOwner
	 *            The identity of an owner of the job.
	 * 
	 * @return True if the caller has an identity that matches the indicated job owner, false otherwise.
	 * 
	 * @throws AuthZSecurityException
	 */
	static public boolean isOwner(Collection<Identity> callers, Identity jobOwner) throws AuthZSecurityException
	{
		if (jobOwner == null)
			return true;

		if (callers.contains(jobOwner))
			return true;

		return false;
	}

	/**
	 * Converts a list of Identities into an array of byte arrays. We do this so that they identities can easily be serialized into a SOAP
	 * message.
	 * 
	 * @param identities
	 *            The list of identieis to convert.
	 * 
	 * @return An array of byte arrays that represents the serialized versions of the listed identities.
	 * 
	 * @throws IOException
	 */
	static public byte[][] convert(Collection<Identity> identities) throws IOException
	{
		byte[][] ret = new byte[identities.size()][];
		int lcv = 0;
		for (Identity id : identities) {
			ret[lcv++] = DBSerializer.serialize(id, Long.MAX_VALUE);
		}

		return ret;
	}

	/*
	 * Determines if caller is admin of this queue. User is a queue admin if they are admin of container Or in the write acl of the queue.
	 */
	public static boolean isQueueAdmin() throws AuthZSecurityException
	{
		if (Security.isAdministrator())
			return true;
		try {
			// 2019-07-21 by ASG. Changed the parameter below to NOT filter out group IDs. If the user is in a group with write access
			// on the queue, then they should have QueueAdmin permissions.
			Collection<Identity> callers = QueueSecurity.getCallerIdentities(false);

			ResourceKey rKey = ResourceManager.getCurrentResource();
			IAuthZProvider authZHandler = AuthZProviders.getProvider(rKey.getServiceName());
			AuthZConfig config = null;
			if (authZHandler != null) {
				config = authZHandler.getAuthZConfig(rKey.dereference(), false);
				Acl resourceAcls = AxisAcl.decodeAcl(config);
				for (AclEntry entry : resourceAcls.writeAcl) {
					if (entry == null) {
						// null entry indicates all access.
						return true;
					}
					for (Identity caller : callers) {
						if (entry.isPermitted(caller)) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			throw new AuthZSecurityException("Unable to load queue Administrators", e);

		}
		return false;
	}
}