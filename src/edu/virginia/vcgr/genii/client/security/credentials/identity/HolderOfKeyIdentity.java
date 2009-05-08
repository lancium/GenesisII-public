package edu.virginia.vcgr.genii.client.security.credentials.identity;

import java.security.cert.X509Certificate;

/**
 * An identity that can be cryptographically authenticated by proving
 * ownership of a secret/private key.
 *  
 * @author dgm4d
 *
 */
public interface HolderOfKeyIdentity extends Identity
{

	public X509Certificate[] getAssertingIdentityCertChain();

}
