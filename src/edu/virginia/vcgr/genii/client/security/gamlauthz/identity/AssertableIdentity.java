package edu.virginia.vcgr.genii.client.security.gamlauthz.identity;

import java.security.cert.X509Certificate;

public interface AssertableIdentity extends Identity {

	public X509Certificate[] getAssertingIdentityCertChain();

}
