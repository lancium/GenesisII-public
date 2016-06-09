/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package edu.virginia.vcgr.genii.container.security.authz.providers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.algorithm.application.ProgramTools;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.InstallationProperties;
import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIACLManager;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.axis.AxisAcl;
import edu.virginia.vcgr.genii.client.sync.VersionVector;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.common.security.AclEntryListType;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.sync.VersionedResourceUtils;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.PublisherTopic;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.TopicSet;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.acl.Acl;
import edu.virginia.vcgr.genii.security.acl.AclEntry;
import edu.virginia.vcgr.genii.security.axis.MessageLevelSecurityRequirements;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.security.identity.Identity;
import edu.virginia.vcgr.genii.security.rwx.RWXManager;

/**
 * AuthZ provider implementation of GII Acls: an access-control mechanism comprised of read/write/execute policy-sets.
 * 
 * NOTES: - The presence of a NULL certificate in the ACL indicates open access. - A NULL ACL indicates no access
 * 
 * @author dmerrill
 */
public class AclAuthZProvider implements IAuthZProvider, AclTopics
{
	/*
	 * cak: this one seems too dangerous to change over to a SAML-named counterpart. it is the name under which all the Acl information is
	 * stored in any grid we would want to migrate.
	 */
	static public final String GENII_ACL_PROPERTY_NAME = "genii.container.security.authz.gaml-acl";

	static protected final MessageLevelSecurityRequirements _defaultMinMsgSec =
		new MessageLevelSecurityRequirements(MessageLevelSecurityRequirements.NONE);

	static protected HashMap<String, X509Certificate> _defaultCertCache = new HashMap<String, X509Certificate>();

	static private Log _logger = LogFactory.getLog(AclAuthZProvider.class);

	/*
	 * NOTE: 'static' below is an implementation change--we are only scanning the default owners once now. we have already been telling people
	 * to stop the container if they want to update the admin certificate or default owners...
	 */
	static private X509Certificate[] _defaultInitialResourceOwners = null;

	public AclAuthZProvider() throws AuthZSecurityException, IOException
	{
		if (_defaultInitialResourceOwners == null) {
			Collection<File> ownerFiles = InstallationProperties.getInstallationProperties().getDefaultOwnerFiles();
			// Installation.getDeployment(new DeploymentName()).security().getDefaultOwnerFiles();

			// read in the certificates that are to serve as default owner
			if ((ownerFiles != null) && (ownerFiles.size() > 0)) {
				synchronized (_defaultCertCache) {
					Collection<X509Certificate> ownerCerts = new ArrayList<X509Certificate>(ownerFiles.size());

					for (File ownerFile : ownerFiles) {
						/*
						 * skip files of the wrong type. this is not a definitive list, but we have seen problems with people storing these
						 * particular types in the default owners folder.
						 */
						if (ownerFile.getName().endsWith(".pfx") || ownerFile.getName().endsWith(".txt")
							|| ownerFile.getName().startsWith(".")) {
							continue;
						}
						// and skip directories, since they are not files...
						if (ownerFile.isDirectory())
							continue;
						// try to lookup that certificate in our cache.
						X509Certificate ownerCert = _defaultCertCache.get(ownerFile.getName());
						if (ownerCert == null) {
							// we didn't have it yet, so load it now.
							try {
								CertificateFactory cf = CertificateFactory.getInstance("X.509");
								FileInputStream fin = null;
								try {
									// load the cert from the file, if possible, and stash it in the cache.
									ownerCert = (X509Certificate) cf.generateCertificate(fin = new FileInputStream(ownerFile));
									_defaultCertCache.put(ownerFile.getName(), ownerCert);
								} finally {
									StreamUtils.close(fin);
								}
							} catch (GeneralSecurityException e) {
								throw new AuthZSecurityException(e.getLocalizedMessage(), e);
							}
						}
						ownerCerts.add(ownerCert);
						if (_logger.isDebugEnabled())
							_logger
								.debug("default resource owner certificate in " + ownerFile + " added with DN: " + ownerCert.getSubjectDN());
					}

					_defaultInitialResourceOwners = ownerCerts.toArray(new X509Certificate[ownerCerts.size()]);
				}
			} else {
				_defaultInitialResourceOwners = new X509Certificate[] { Container.getContainerCertChain()[0] };
				if (_logger.isDebugEnabled())
					_logger.warn("no owner certificate was found for the container");

			}
		}
	}

	/**
	 * Presently configures the specified resource to have default access allowed for every credential in the bag of credentials. We may want
	 * to look at restricting this in the future to special credentials.
	 */
	public void setDefaultAccess(ICallingContext callingContext, IResource resource, X509Certificate[] serviceCertChain)
		throws AuthZSecurityException, ResourceException
	{
		HashSet<Identity> defaultOwners = new HashSet<Identity>();

		// Add desired authorized identities from incoming cred
		if (callingContext != null) {
			TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callingContext);

			for (NuCredential cred : transientCredentials.getCredentials()) {

				if (cred instanceof Identity) {
					if (_logger.isTraceEnabled())
						_logger.trace("adding identity to UMask... " + cred.describe(VerbosityLevel.HIGH));
					if (((Identity) cred).placeInUMask())
						defaultOwners.add((Identity) cred);
				} else if (cred instanceof TrustCredential) {
					X509Identity assertedIdentity = ((TrustCredential) cred).getRootIdentity();
					if (assertedIdentity.placeInUMask()) {
						if (_logger.isTraceEnabled())
							_logger.trace("adding cred to UMask... " + assertedIdentity.describe(VerbosityLevel.HIGH));
						defaultOwners.add(assertedIdentity);
					} else {
						if (_logger.isTraceEnabled())
							_logger.trace("NOT adding cred to UMask... " + assertedIdentity.describe(VerbosityLevel.HIGH));
					}

				} else {
					if (_logger.isTraceEnabled())
						_logger.trace("NOT adding this thing to UMask... " + cred.describe(VerbosityLevel.HIGH));
				}
			}
		}

		// if no incoming credentials, use the default owner identity indicated
		// by the container
		if (defaultOwners.isEmpty()) {
			if (_defaultInitialResourceOwners != null) {
				for (X509Certificate cert : _defaultInitialResourceOwners) {
					defaultOwners.add(new X509Identity(new X509Certificate[] { cert }));
				}
			}
		}

		/*
		 * October 15, 2015. ASG and CAK Added code to allow invokers to pass in rwx mask for access control.
		 */
		ICallingContext context;
		String mask = null;
		try {
			context = ContextManager.getCurrentContext();
			mask = (String) context.getSingleValueProperty(GenesisIIConstants.CREATION_MASK);
		} catch (Exception e) {
			if (_logger.isTraceEnabled())
				_logger.debug("setDefaultAccess could not acquire calling context to retrieve creation mask");
		}
		if (mask == null)
			mask = "rwx";

		Acl acl = new Acl();
		if (mask.contains("r"))
			acl.readAcl.addAll(defaultOwners);
		if (mask.contains("w"))
			acl.writeAcl.addAll(defaultOwners);
		if (mask.contains("x"))
			acl.executeAcl.addAll(defaultOwners);
		/*
		 * dgm4d: this is a security issue // Add the resoure's static creating service if (serviceCertChain != null) { defaultOwners.add(new
		 * X509Identity(serviceCertChain)); }
		 */

		// Add the resource itself
		// Commented and modified by ASG 2016/04/13 to use the container cert instead
		/*
		 * X509Identity res = new X509Identity((X509Certificate[]) resource.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME));
		 */
		X509Identity res = new X509Identity(Container.getContainerCertChain());
		acl.readAcl.add(res);
		acl.writeAcl.add(res);
		acl.executeAcl.add(res);

		if (resource instanceof BasicDBResource) {
			try {
				((BasicDBResource) resource).setAclMatrix(acl, true);
			} catch (SQLException e) {
				_logger.debug("Could not set ACL for object");
			}
		} else {
			resource.setProperty(GENII_ACL_PROPERTY_NAME, acl);
		}
	}

	// private boolean checkAclAccess(Identity identity, RWXCategory category, Acl acl)
	// {
	// Collection<AclEntry> trustList = null;
	// switch (category) {
	// case READ:
	// trustList = acl.readAcl;
	// break;
	// case WRITE:
	// trustList = acl.writeAcl;
	// break;
	// case EXECUTE:
	// trustList = acl.executeAcl;
	// break;
	// case OPEN:
	// // /hmmm: could measure this with grant check if changed wording.
	// if (_logger.isDebugEnabled())
	// _logger.debug("giving access to identity due to OPEN permission: "
	// + ((identity != null) ? identity.describe(VerbosityLevel.HIGH) : "null"));
	// return true;
	// case CLOSED:
	// return false;
	// case OWNER:
	// _logger.debug("OWNER flag seen, returning false since UNIMPLEMENTED");
	// return false;
	// case APPEND:
	// _logger.debug("APPEND flag seen, returning false since UNIMPLEMENTED");
	// return false;
	// case INHERITED:
	// _logger.warn("unprocessed case for inherited attribute.");
	// }
	// if (trustList == null) {
	// // Empty ACL
	// _logger.error("failing ACL access check due to null trust list.");
	// return false;
	// } else if (trustList.contains(null)) {
	// // ACL contains null (the wildcard certificate)
	// if (_logger.isTraceEnabled())
	// _logger.debug("passing ACL access check due to wildcard in trust list");
	// return true;
	// } else {
	// // go through the AclEntries
	// for (AclEntry entry : trustList) {
	// try {
	// if (entry.isPermitted(identity)) {
	// if (_logger.isTraceEnabled())
	// _logger.debug("passing ACL access check due to permission (for " + entry.toString() + ") on identity: "
	// + ((identity != null) ? identity.describe(VerbosityLevel.HIGH) : "null"));
	// return true;
	// }
	// } catch (Exception e) {
	// _logger.error("caught exception coming from isPermitted check on identity: " + identity.toString());
	// }
	// }
	// }
	// if (_logger.isTraceEnabled())
	// _logger.trace("bailing on ACL access check after exhausting all options.");
	// return false;
	// }

	@Override
	public boolean checkAccess(Collection<NuCredential> authenticatedCallerCredentials, IResource resource, Class<?> serviceClass,
		Method operation)
	{
		RWXCategory category = RWXManager.lookup(serviceClass, operation);
		if (!checkAccess(authenticatedCallerCredentials, resource, category)) {
			String msg = "denying access for operation: " + operation.getName();
			String asset = ResourceManager.getResourceName(resource);
			msg = msg.concat(" on '" + asset + "'");
			_logger.info(msg);

			if (_logger.isDebugEnabled())
				_logger.debug("failed access attempt had these credentials: "
					+ TrustCredential.showCredentialList(authenticatedCallerCredentials, VerbosityLevel.HIGH));

			return false;
		}
		return true;
	}

	public void blurtCredentials(String prefixedMessage, Collection<NuCredential> creds)
	{
		if (!_logger.isDebugEnabled())
			return; // no printing unless debug mode allowed.
		StringBuilder credsAsString = new StringBuilder(prefixedMessage);
		credsAsString.append("\n");
		for (NuCredential cred : creds) {
			credsAsString.append("\n----\n");
			credsAsString.append(cred.toString());
			credsAsString.append("\n----\n");
		}
		_logger.debug(credsAsString);
	}

	public boolean checkX509Permission(Identity identity, String ACLString, String idEPI, char c) throws GeneralSecurityException
	{
		int epiPos = ACLString.indexOf(idEPI);
		if (epiPos >= 0) {
			// Now extract the permissions string for this EPI into a string
			String permissionString = GenesisIIACLManager.extractPermissions(ACLString, idEPI);
			// System.out.println("Permission for " +idEPI + "is " + permissionString);
			// Check if requested access is in the permissions string
			// First see if the RWX is also correct.
			if (permissionString.indexOf(c) >= 0) {
				// It was found in the ACLString. That alone is not sufficient. We MUST verify the keys
				AclEntry entry = BasicDBResource.getPrincipalfromDB(idEPI);
				if (entry.isPermitted(identity)) {
					// System.out.println("Permission GRANTED " +idEPI + " is " + permissionString + ", Don't forget to check the keys");
					return true;
				} else {
					if (_logger.isDebugEnabled())
						_logger
							.debug("****************w8609w548609e46*************************************** Keys did not match for." + idEPI);
					return true;
				}

			} else
				return false; // This permission is not in the permission string
		} else
			return false; // Not in the ACLString
	}

	public boolean checkAclAccess(Identity identity, RWXCategory category, String ACLString, IResource iresource)
		throws GeneralSecurityException
	{
		// This is new by ASG 1/7/2016
		/*
		 * There are four possible prefixes for principals in the ACL string: ws-nameing-epi:, SN:, GUID:, and username-password, and three
		 * prefixes that getEPI can return: ws-naming-epi: SN:, and username-password. A ws-naming prefix is one of our identities. A SN
		 * identity is an X.509, a GUID: is a pattern-based ACL, and a username password is just that.
		 */
		// First turn the identity into a string we can search in the ACLString for
		String idEPI = identity.getEPI(false);
		char c = RWXCategory.convertToChar(category);

		if (idEPI == null)
			return false;
		if (_logger.isTraceEnabled())
			_logger.debug("About to check access for " + idEPI + "/ for operaton " + category.name() + ", aclstring= " + ACLString);
		if ((idEPI.indexOf("ws-naming:epi:") >= 0)) {
			return checkX509Permission(identity, ACLString, idEPI, c);
		} else if (idEPI.indexOf("SN:") >= 0) {
			// It is a plain X.509. There are two cases: it is in the ACL string, or there may be
			// a pattern entry in the ACLString - indicated with a "GUID",
			if (checkX509Permission(identity, ACLString, idEPI, c))
				return true;
			// OK, it is not in there as a straight X.509, now look for a pattern
			// For the GUID case we need a different approach.
			// Gather all X.509 pattern EPIs from the ACLString
			List<AclEntry> guidEntries = new ArrayList<AclEntry>();
			String remainder = ACLString;
			int guidPos = ACLString.indexOf("GUID:");
			int colonPos = ACLString.indexOf(';');
			if ((guidPos < 0) || (colonPos < 0))
				return false;
			String currentGUID = ACLString.substring(guidPos, colonPos);
			while (!(currentGUID == null || currentGUID.length() == 0)) {
				// While there are no more guids in the ACLString
				String lookupEPI = currentGUID.substring(0, currentGUID.indexOf(' '));
				guidEntries.add(BasicDBResource.getPrincipalfromDB(lookupEPI));
				remainder = remainder.substring(remainder.indexOf(';') + 1);
				guidPos = remainder.indexOf("GUID:");
				colonPos = remainder.indexOf(';');
				if ((guidPos < 0) || (colonPos < 0))
					break;
				currentGUID = remainder.substring(guidPos, colonPos);
			}
			// For each X509 pattern entry EPI, getPrincipalFromDB - returns an AclEntry
			for (AclEntry acl : guidEntries) {
				if (acl.isPermitted(identity))
					return true;
			}
			return false;
		} else if (idEPI.indexOf(UsernamePasswordIdentity.USER_NAME_PASSWD_EPI) >= 0) {
			int epiPos =
				ACLString.indexOf(UsernamePasswordIdentity.USER_NAME_PASSWD_EPI + ((UsernamePasswordIdentity) identity).getUserName());
			if (epiPos >= 0) {
				// First extract the username and hashed password from the ACLstring
				String aclpart = ACLString.substring(epiPos, ACLString.indexOf(';', epiPos));
				String uname = aclpart.substring(aclpart.lastIndexOf(':') + 1, aclpart.indexOf('/'));
				String password = aclpart.substring(aclpart.indexOf('/') + 1, aclpart.indexOf(' '));
				// If these strings are not in the ACL string, then return false;
				if ((password == null) || (uname == null) || (password == null))
					return false;
				UsernamePasswordIdentity aclid = new UsernamePasswordIdentity(uname, password);
				try {
					if (aclid.isPermitted(identity)) {
						// Now extract the permissions string for this EPI into a string
						String permissionString = GenesisIIACLManager.extractPermissions(ACLString, aclid.getEPI(false));
						// System.out.println("Permission for " +idEPI + "is " + permissionString);
						// Check if requested access is in the permissions string
						// First see if the RWX is also correct.
						if (permissionString.indexOf(c) >= 0) {
							// It was found in the ACLString. That alone is not sufficient. We MUST verify the keys.
							// System.out.println("Permission GRANTED " +idEPI + " is " + permissionString + ", Don't forget to check the
							// keys");
							return true;
						} else
							return false; // This permission is not in the permission string
					}
				} catch (GeneralSecurityException e) {
					return false;
				}
			} else
				return false; // Not in the ACLString
		}
		return false;
	}

	@Override
	public boolean checkAccess(Collection<NuCredential> authenticatedCallerCredentials, IResource iresource, RWXCategory category)
	{
		String messagePrefix = "checkAccess for " + category + " on " + ResourceManager.getResourceName(iresource) + " ";
		String ACLString = null;
		try {
			ICallingContext callContext = ContextManager.getExistingContext();
			// 1/5/2016 ASG This is the code for the new access control list implementation

			if (iresource instanceof BasicDBResource) {
				// Problem ... may contain a BasicDBResource and not be an instance of
				BasicDBResource resource = (BasicDBResource) iresource;
				ACLString = resource.getACLString(false);
				if (ACLString == null) {
					// This is the case were we need to convert from old format to new
					if (resource.translateOldAcl()) {
						// Now that we know all of the old ACL entries are in the database, get aclstring again
						ACLString = resource.getACLString(false);
					}

					if (ACLString == null) {
						if (_logger.isDebugEnabled())
							_logger.debug(messagePrefix + "failed due to missing access control list or inability to translate old ACL.");
						return false;
					}

				}
			} else {
				ACLString = iresource.getACLString(false);
				if (ACLString == null)
					return false; // We cannot check access without an acl string
			}

			// Acl acl = (Acl) resource.getProperty(GENII_ACL_PROPERTY_NAME);

			// Do not know what to do about OPEN access .. how do we store that in an ACL. How do I know if it is there?

			// First let's see if EVERYONE can do this, we can save a lot of time
			char c = RWXCategory.convertToChar(category);
			if (c == '*') {
				// This means the method is open to anyone
				System.out.println("An OPEN method");
				if (_logger.isDebugEnabled())
					_logger.debug(messagePrefix + "granted to everyone, method is OPEN.");
				return true;
			}
			if (ACLString.indexOf("EVERYONE") >= 0) {
				if (GenesisIIACLManager.extractPermissions(ACLString, "EVERYONE").indexOf(c) >= 0) {
					System.out.println("EVERYONE is allowed access");
					if (_logger.isDebugEnabled())
						_logger.debug(messagePrefix + "granted to everyone.");
					return true;
				}
			}

			// try each identity in the caller's credentials.
			for (NuCredential cred : authenticatedCallerCredentials) {
				try {
					if (cred instanceof Identity) {

						if (cred instanceof X509Identity) {
							if (((X509Identity) cred).checkRWXAccess(category)) {
								if (checkAclAccess((Identity) cred, category, ACLString, iresource)) {
									// We need to verify the key

									if (_logger.isDebugEnabled())
										_logger.debug(messagePrefix + "granted to identity with x509: " + cred.describe(VerbosityLevel.LOW));
									return true;
								}
							}
						} else if (checkAclAccess((Identity) cred, category, ACLString, iresource)) {
							// straight-up identity (username/password)
							if (_logger.isDebugEnabled())
								_logger.debug(messagePrefix + "access granted to identity: " + cred.describe(VerbosityLevel.LOW));
							return true;
						}
					} else if (cred instanceof TrustCredential) {
						TrustCredential sa = (TrustCredential) cred;
						if (sa.checkRWXAccess(category)) {
							// check root identity of trust delegation to see if it has access.
							X509Identity ia = (X509Identity) sa.getRootIdentity();
							if (checkAclAccess(ia, category, ACLString, iresource)) {
								if (_logger.isDebugEnabled())
									_logger.debug(
										messagePrefix + "granted to trust credential's root identity: " + sa.describe(VerbosityLevel.LOW));
								return true;
							}
						}
					}
				} catch (GeneralSecurityException e) {
					// Don't do anything, just keep going, another cred may get them through

				}
			}

			// Check Administrator Access
			if (Security.isAdministrator(callContext)) {
				if (_logger.isDebugEnabled())
					_logger.debug(messagePrefix + "granted because caller is admin.");
				return true;
			}

			// Nobody appreciates us.
			if (_logger.isTraceEnabled()) {
				blurtCredentials(messagePrefix + " was not granted for credential set: ", authenticatedCallerCredentials);
			}
			return false;
		} catch (AuthZSecurityException ase) {
			_logger.error("failure, saw authorization security exception for " + messagePrefix + ":" + ase.getMessage());
			return false;
		} catch (IOException e) {
			_logger.error("failure, saw IO exception processing credential for " + messagePrefix + ":" + e.getMessage());
			return false;
		} catch (ConfigurationException e) {
			_logger.error("saw config exception for " + messagePrefix + ":" + e.getMessage());
			return false;
		} catch (SQLException e) {
			_logger.error("saw SQL exception during translateOldACLfor " + messagePrefix + ":" + e.getMessage());
			return false;
		}
	}

	public MessageLevelSecurityRequirements getMinIncomingMsgLevelSecurity(IResource iresource)
		throws AuthZSecurityException, ResourceException
	{
		try {
			// get ACL
			Acl acl = null;
			if (iresource instanceof BasicDBResource) {
				BasicDBResource resource = (BasicDBResource) iresource;
				acl = resource.getAcl();
			} else {
				acl = (Acl) iresource.getProperty(GENII_ACL_PROPERTY_NAME);
			}
			if (acl == null) {

				// return no security requirements if null ACL
				return new MessageLevelSecurityRequirements(MessageLevelSecurityRequirements.NONE);

			} else if (acl.requireEncryption) {

				// add in encryption
				return _defaultMinMsgSec.computeUnion(new MessageLevelSecurityRequirements(MessageLevelSecurityRequirements.ENCRYPT));
			}

			return _defaultMinMsgSec;
		} catch (ResourceException e) {
			throw new AuthZSecurityException("Could not retrieve minimum incoming message level security.", e);
		} catch (SQLException e) {
			throw new AuthZSecurityException("Could not retrieve minimum incoming message level security.", e);
		}
	}

	public AuthZConfig getAuthZConfig(IResource resource) throws AuthZSecurityException, ResourceException
	{
		return getAuthZConfig(resource, true);
	}

	public AuthZConfig getAuthZConfig(IResource iresource, boolean sanitize) throws AuthZSecurityException, ResourceException
	{
		try {
			// get ACL
			// Acl acl = (Acl) resource.getProperty(GENII_ACL_PROPERTY_NAME);
			Acl acl = null;
			BasicDBResource resource = (BasicDBResource) iresource;
			acl = resource.getAcl();
			/*
			 * So now instead we need to resource.getAclString(); if null - translate aclstring, then getaclstring Acl acl = new
			 * Acl(aclString)
			 * 
			 * String aclString=resource.getACLString(false); if (aclString==null) { resource.translateOldAcl();
			 * aclString=resource.getACLString(false); } if (aclString!=null){ System.out.println("TRANSLATING AN ACL STRING INTO AN ACL");
			 * acl= resource.aclStringToAcl(aclString); }
			 */
			if (acl != null) {
				return AxisAcl.encodeAcl(acl, true);
			}

			return new AuthZConfig(null);

		} catch (ResourceException e) {
			throw new AuthZSecurityException("Unable to load AuthZ config.", e);
		} catch (SQLException e) {
			throw new AuthZSecurityException("Unable to translate AuthZ config.", e);
		}
	}

	public void setAuthZConfig(AuthZConfig config, IResource resource) throws AuthZSecurityException, ResourceException
	{
		Acl acl = AxisAcl.decodeAcl(config);
		if (resource instanceof BasicDBResource) {
			try {
				((BasicDBResource) resource).setAclMatrix(acl, true);
			} catch (SQLException e) {
				_logger.debug("Could not set ACL for object");
			}
		} else {
			resource.setProperty(GENII_ACL_PROPERTY_NAME, acl);
		}
		resource.commit();
	}

	/**
	 * Inform subscribers that one or more entries has been added or removed from one or more ACLs.
	 */
	public void sendAuthZConfig(AuthZConfig oldConfig, AuthZConfig newConfig, IResource resource)
		throws AuthZSecurityException, ResourceException
	{
		if (resource.isServiceResource())
			return;
		Acl oldAcl = AxisAcl.decodeAcl(oldConfig);
		if (oldAcl == null)
			oldAcl = new Acl();
		Acl newAcl = AxisAcl.decodeAcl(newConfig);
		if (newAcl == null)
			newAcl = new Acl();
		List<AclEntry> entryList = new ArrayList<AclEntry>();
		List<String> tagList = new ArrayList<String>();
		findDeltas(entryList, tagList, newAcl.readAcl, oldAcl.readAcl, "r");
		findDeltas(entryList, tagList, newAcl.writeAcl, oldAcl.writeAcl, "w");
		findDeltas(entryList, tagList, newAcl.executeAcl, oldAcl.executeAcl, "x");
		if (entryList.size() > 0) {
			VersionVector vvr = VersionedResourceUtils.incrementResourceVersion(resource);
			TopicSet space = TopicSet.forPublisher(getClass());
			PublisherTopic publisherTopic = space.createPublisherTopic(GENII_ACL_CHANGE_TOPIC);
			AclEntryListType entryArray = AxisAcl.encodeIdentityList(entryList, false);
			String[] tagArray = tagList.toArray(new String[0]);
			publisherTopic.publish(new AclChangeContents(entryArray, tagArray, vvr));
		}
	}

	private static void findDeltas(List<AclEntry> entryList, List<String> tagList, Collection<AclEntry> newAcl, Collection<AclEntry> oldAcl,
		String mode)
	{
		if (newAcl == null)
			newAcl = new ArrayList<AclEntry>();
		if (oldAcl == null)
			oldAcl = new ArrayList<AclEntry>();
		Iterator<AclEntry> newIter = newAcl.iterator();
		while (newIter.hasNext()) {
			AclEntry entry = newIter.next();
			boolean found = false;
			Iterator<AclEntry> oldIter = oldAcl.iterator();
			while ((!found) && oldIter.hasNext()) {
				AclEntry oldEntry = oldIter.next();
				if (((entry == null) && (oldEntry == null)) || ((entry != null) && entry.equals(oldEntry))) {
					found = true;
					oldIter.remove();
				}
			}
			if (!found) {
				if (_logger.isTraceEnabled())
					_logger.trace("adding acl to list: " + entry == null ? "wildcard" : entry);
				entryList.add(entry);
				tagList.add("+" + mode);
			}
		}
		Iterator<AclEntry> oldIter = oldAcl.iterator();
		while (oldIter.hasNext()) {
			AclEntry oldEntry = oldIter.next();
			if (_logger.isTraceEnabled())
				_logger.trace("removing acl from list " + oldEntry == null ? "wildcard" : oldEntry);
			entryList.add(oldEntry);
			tagList.add("-" + mode);
		}
	}

	/**
	 * Update the resource ACLs with the changes that have been applied to a replica.
	 */
	public void receiveAuthZConfig(NotificationMessageContents message, IResource resource) throws ResourceException, AuthZSecurityException
	{
		Acl acl = null;
		if (resource instanceof BasicDBResource) {
			try {
				acl = ((BasicDBResource) resource).getAcl();
			} catch (SQLException e) {
				_logger.debug("Could not get ACL for object");
			}
		} else {
			acl = (Acl) resource.getProperty(GENII_ACL_PROPERTY_NAME);
		}

		AclChangeContents contents = (AclChangeContents) message;
		AclEntryListType encodedList = contents.aclEntryList();
		List<AclEntry> entryList = AxisAcl.decodeIdentityList(encodedList);
		int length = entryList.size();
		if (length == 0)
			return;
		String[] tagList = contents.tagList();
		if ((tagList == null) || (tagList.length != length))
			throw new AuthZSecurityException("AclChangeContents " + "entry list does not match tag list");
		for (int idx = 0; idx < length; idx++) {
			AclEntry entry = entryList.get(idx);
			String tag = tagList[idx];
			AxisAcl.chmod(acl, tag, entry);
		}
		if (resource instanceof BasicDBResource) {
			try {
				((BasicDBResource) resource).setAclMatrix(acl, true);
			} catch (SQLException e) {
				_logger.debug("Could not set ACL for object");
			}
		} else {
			resource.setProperty(GENII_ACL_PROPERTY_NAME, acl);
		}
		resource.commit();
	}
}
