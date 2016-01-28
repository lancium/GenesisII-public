package edu.virginia.vcgr.genii.client.security;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

/**
 * a data capsule for the preferred identity feature. this is what we can lookup in the calling context to find out whether the user has a
 * preferred identity set or not.
 */
public class PreferredIdentity implements Serializable
{
	private static final long serialVersionUID = 1L;

	/*
	 * note: this class is serializable only for backwards compatibility; do not serialize it for any purposes now-a-days.
	 */

	static private Log _logger = LogFactory.getLog(PreferredIdentity.class);

	static public final String PREFERRED_IDENTITY_PROPERTY_NAME = "PreferredIdentity";

	/*
	 * the current value of the preferred identity; this is sought in the credentials as a USER or CONNECTION type. the server-side (gffs
	 * container) makes the final call about whether the user has the right to become that identity, based on the user's current set of
	 * credentials.
	 */
	private String _identityString = null;

	/*
	 * a hint for the client-side that the user doesn't care if the identity is not present in the current set of credentials and that they
	 * wish to use it anyway. this means that the logout command doesn't re-acquire a preferred identity. it also means that, in the case of
	 * that identity not being in the current set of credentials, the server side will just use the first "USER" style identity available in
	 * the credentials.
	 */
	private Boolean _fixateIdentity = null;

	public PreferredIdentity()
	{
	}

	/**
	 * convenience method that takes the known members.
	 */
	public PreferredIdentity(String idString, boolean fixate)
	{
		_identityString = idString;
		_fixateIdentity = fixate;
	}

	@Override
	public String toString()
	{
		if (_identityString == null)
			return "null";
		return _identityString;
	}

	/**
	 * return the current preferred identity in openssl one-line RDN format.
	 */
	public String getIdentityString()
	{
		return _identityString;
	}

	public void setIdentityString(String newPreferredIdentity)
	{
		_identityString = newPreferredIdentity;
	}

	public Boolean getFixateIdentity()
	{
		return _fixateIdentity;
	}

	public void setFixateIdentity(Boolean fixateIdentity)
	{
		_fixateIdentity = fixateIdentity;
	}

	/**
	 * returns true if the identity held in this object matches the certificate in "toCheck". the identity equality will be based on the
	 * OpenSSL one-line RDN format.
	 */
	public boolean matchesIdentity(X509Certificate toCheck)
	{
		if ((toCheck == null) && (_identityString == null))
			return true;
		if ((toCheck == null) || (_identityString == null))
			return false;
		return _identityString.equals(PreferredIdentity.getDnString(toCheck));
	}

	/**
	 * a helper method for getting the right type of DN out of the x509 certificate.
	 */
	public static String getDnString(X509Certificate target)
	{
		if (target == null)
			return null;
		return X509Identity.getOpensslRdn(target);
	}

	// static methods for managing current context...

	/**
	 * returns true if there is a preferred identity set in the current calling context.
	 */
	static public boolean existsInCurrent()
	{
		PreferredIdentity current = getCurrent();
		return current != null;
	}

	/**
	 * returns true if the current calling context has a preferred identity set *and* that identity is fixated by user request.
	 */
	static public boolean fixatedInCurrent()
	{
		PreferredIdentity current = getCurrent();
		if (current == null)
			return false;
		return current._fixateIdentity;
	}

	/**
	 * returns the preferred identity that's set in the current calling context. this will return null if no identity is set.
	 */
	static public PreferredIdentity getCurrent()
	{
		try {
			return getFromContext(ContextManager.getCurrentContext());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * throws out the current preferred identity, if set.
	 */
	static public void dropCurrent()
	{
		try {
			removeFromContext(ContextManager.getCurrentContext());
		} catch (Exception e) {
			// ignoring this, since maybe there's just no context.
		}
	}

	// static methods operating on specific context...

	/**
	 * loads the preferred identity object from the context, if possible. if it's not there, null is returned.
	 */
	static public PreferredIdentity getFromContext(ICallingContext context)
	{
		if (context == null)
			return null;
		try {
			Object prefChunk = context.getSingleValueProperty(PREFERRED_IDENTITY_PROPERTY_NAME);
			// if nothing's there, just bail out.
			if (prefChunk == null)
				return null;
			if (prefChunk instanceof String) {
				_logger.debug("found preferred identity blob in calling context:" + prefChunk.toString());
				return PreferredIdentity.decodePrefId((String) prefChunk);
			} else if (prefChunk instanceof PreferredIdentity) {
				/*
				 * older school version. we will trust that it's still the right format. but next time we store it, it goes in as a string.
				 */
				return (PreferredIdentity) prefChunk;
			} else {
				_logger.debug(
					"got something called a preferred identity in calling context, but it's the wrong type of object!  object type is: "
						+ prefChunk.getClass().getCanonicalName());
				return null;
			}
		} catch (Exception e) {
			_logger.debug("found no preferred identity in calling context");
			return null;
		}
	}

	/**
	 * returns the encoded form of the preferred identity for putting into the context.
	 */
	public String encodePrefId()
	{
		StringBuilder toReturn = new StringBuilder();
		toReturn.append("fix=" + _fixateIdentity + ",dn=" + _identityString + ":");
		return toReturn.toString();
	}

	static public PreferredIdentity decodePrefId(String encoded)
	{
		String fail = "decoding failure for PreferredIdentity: ";
		if (!encoded.startsWith("fix=")) {
			_logger.error(fail + "fixation flag missing");
			return null;
		}
		// chop fix= off of string.
		encoded = encoded.substring(4);
		int commaPosn = encoded.indexOf(",");
		if (commaPosn < 0) {
			_logger.error(fail + "missing separator after fixation flag");
			return null;
		}
		String justFixated = encoded.substring(0, commaPosn);
		if (_logger.isDebugEnabled())
			_logger.debug("got fixation flag of: " + justFixated);
		boolean fixated = Boolean.valueOf(justFixated);
		encoded = encoded.substring(commaPosn + 1);
		if (!encoded.startsWith("dn=")) {
			_logger.error(fail + "DN flag missing for identity string");
			return null;
		}
		// skip "dn=" bit and terminating colon, remainder is identity DN.
		String ident = encoded.substring(3, encoded.length() - 1);
		if (_logger.isDebugEnabled())
			_logger.debug("decoded identity: '" + ident + "'");
		return new PreferredIdentity(ident, fixated);
	}

	/**
	 * stores a preferred identity in the context provided. this stores the context also to make sure the change persists.
	 */
	static public void setInContext(ICallingContext context, PreferredIdentity newIdentity)
	{
		if (context == null)
			return;

		try {
			removeFromContext(context);

			String encoded = newIdentity.encodePrefId();

			// pop in the new version.
			context.setSingleValueProperty(PREFERRED_IDENTITY_PROPERTY_NAME, encoded);
			ContextManager.storeCurrentContext(context);
		} catch (Exception e) {
			_logger.debug("could not store preferred identity in calling context", e);
		}
	}

	/**
	 * trashes any existing preferred identity in the context.
	 */
	static public void removeFromContext(ICallingContext context)
	{
		if (context == null)
			return;

		// zap any prior version.
		context.removeProperty(PREFERRED_IDENTITY_PROPERTY_NAME);
		try {
			ContextManager.storeCurrentContext(context);
		} catch (Exception e) {
			_logger.error("failed to store context", e);
		}
	}

	// helper methods...

	/**
	 * creates a list of credentials for the current client's state, and adds a credential for the connection as an X509Identity. this should
	 * only be needed on the client; the container should be able to use its list of authorized credentials instead.
	 */
	public static List<NuCredential> gatherCredentials(ICallingContext callingContext) throws AuthZSecurityException
	{
		List<NuCredential> toReturn = new ArrayList<NuCredential>();

		// get the connection info first.
		KeyAndCertMaterial clientKeyMaterial =
			ClientUtils.checkAndRenewCredentials(callingContext, BaseGridTool.credsValidUntil(), new SecurityUpdateResults());
		toReturn.add(new X509Identity(clientKeyMaterial._clientCertChain, IdentityType.CONNECTION));

		// now plunge through the transient credentials that exist.
		TransientCredentials transientCredentials = TransientCredentials.getTransientCredentials(callingContext);
		for (NuCredential cred : transientCredentials.getCredentials()) {
			toReturn.add(cred);
		}

		return toReturn;
	}

	/**
	 * tries to locate a matching identity in the "creds" to the "pattern" provided. if no match is found, then null is returned. note that
	 * the "creds" list must contain all identities that should be examined, including the CONNECTION identity stored as an X509Identity
	 * object. just giving a credential wallet's contents is not enough, since those never include the connection.
	 */
	static public X509Certificate findIdentityPatternInCredentials(String pattern, List<NuCredential> creds)
	{
		if ((pattern == null) || (creds == null))
			return null;

		for (NuCredential nc : creds) {
			if (nc instanceof TrustCredential) {
				TrustCredential tc = (TrustCredential) nc;
				if (tc.getIssuerType() != IdentityType.USER) {
					// we only care about user identities here.
					continue;
				}
			} else if (!(nc instanceof X509Identity)) {
				// if it's not a user or a connection identity (stored as x509 identity), then we
				// don't want it.
				continue;
			}
			X509Certificate[] x509 = nc.getOriginalAsserter();

			if (PreferredIdentity.getDnString(x509[0]).contains(pattern)) {
				return x509[0];
			}
		}
		return null;
	}

	static public String resolveIdentityPatternInCredentials(String pattern, List<NuCredential> creds)
	{
		X509Certificate cert = findIdentityPatternInCredentials(pattern, creds);
		if (cert != null) {
			return PreferredIdentity.getDnString(cert);
		}
		return null;
	}

	/**
	 * helper method that can dump the user and connection DNs to a string.
	 */
	public static String printUserAndConnCredentials(List<NuCredential> creds)
	{
		if (creds == null)
			return null;

		StringBuilder sb = new StringBuilder();
		for (NuCredential nc : creds) {
			if (nc instanceof TrustCredential) {
				TrustCredential tc = (TrustCredential) nc;
				if (tc.getIssuerType() != IdentityType.USER) {
					// we only care about user identities here.
					continue;
				}
			} else if (!(nc instanceof X509Identity)) {
				// if it's not a user or a connection identity (stored as x509 identity), then we
				// don't want it.
				continue;
			}
			X509Certificate[] x509 = nc.getOriginalAsserter();
			sb.append("\"");
			sb.append(PreferredIdentity.getDnString(x509[0]));
			sb.append("\"\n");
		}
		return sb.toString();
	}

}
