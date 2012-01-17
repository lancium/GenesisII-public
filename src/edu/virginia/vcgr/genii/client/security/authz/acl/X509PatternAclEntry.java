package edu.virginia.vcgr.genii.client.security.authz.acl;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import java.util.Vector;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.PrincipalUtil;

import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.X500PrincipalUtilities;
import edu.virginia.vcgr.genii.security.credentials.identity.*;

/**
 * This ACL rule provides a chain of trust that callers must chain to, 
 * and, optionally, a pattern that the caller's principal name must match. 
 * 
 * The pattern allows for a spectrum of principal-grouping possibilities, e.g.:
 * - A null pattern allows for grouping of X.509 principals by the specified 
 *   certification authority
 * - A partial pattern allows for grouping of logically-equivalent 
 *   principals (i.e., to allow for certificates in which the distinguished 
 *   name may vary slightly between versions, e.g., proxy certificates)  
 * 
 * @author dgm4d
 *
 */
public class X509PatternAclEntry implements AclEntry, Serializable {

	static public final long serialVersionUID = 0L;
	
	protected X509Identity _trustRoot;
	protected X500Principal _userPattern;

	// cache a trust manager upon first use
	transient protected X509TrustManager _trustManager;
	
	// cache an X509 pattern upon first use
	transient protected X509Principal _bcPattern;
	
	public X509PatternAclEntry(X509Identity trustRoot, X500Principal userPattern)
	{	
		_userPattern = userPattern;
		_trustRoot = trustRoot;
	}

	synchronized void initPattern() throws GeneralSecurityException {
		if (_bcPattern == null) {
			try {
				_bcPattern = new X509Principal(_userPattern.getEncoded());
			} catch (IOException e) {
				throw new GeneralSecurityException(e.getMessage(), e);
			}
		}
	}
	
	synchronized void initTrustManager() throws GeneralSecurityException, IOException {
		if (_trustManager == null) {

			// create an in-memory cert keystore for the trusted certs
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(null, null);

			// add the trusted cert into the memory-keystore
			X509Certificate trustedCert = _trustRoot.getAuthorizedIdentity()[0];
			ks.setCertificateEntry(trustedCert
					.getSubjectX500Principal().getName(),
					trustedCert);

			// create a trust manager from the key store
			PKIXBuilderParameters pkixParams =
					new PKIXBuilderParameters(ks,
							new X509CertSelector());
			pkixParams.setRevocationEnabled(false);
			ManagerFactoryParameters trustParams =
					new CertPathTrustManagerParameters(pkixParams);
			TrustManagerFactory tmf =
					TrustManagerFactory.getInstance("PKIX");
			tmf.init(trustParams);
			_trustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		}
	}
	
	protected void validateTrust(X509Identity user) throws GeneralSecurityException {
		
		try 
		{
			initTrustManager();
		} 
		catch (IOException e) 
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		} 
			
		X509Certificate[] userCertChain = user.getAuthorizedIdentity();
		_trustManager.checkClientTrusted(
				userCertChain,
				userCertChain[0].getPublicKey().getAlgorithm());
	}
	
	@SuppressWarnings("unchecked")
	protected boolean matches(X509Identity user) throws GeneralSecurityException {
		
		if (_userPattern == null) {
			return true;
		}
		
		X509Certificate userCert = user.getAuthorizedIdentity()[0];
		X509Principal userPrincipal = PrincipalUtil.getSubjectX509Principal(userCert);
		
		// attempt to match each pattern oid with one in the caller's DN
		initPattern();
		Vector<DERObjectIdentifier> oids = _bcPattern.getOIDs();
		for (DERObjectIdentifier oid : oids) {
			Vector<String> patternValues = _bcPattern.getValues(oid);
			Vector<String> userValues = userPrincipal.getValues(oid);
			if ((userValues == null) || (!userValues.containsAll(patternValues))) {
				return false;
			}
		}
		
		return true;
	}
	
	
	@Override
	public boolean isPermitted(Identity identity)
			throws GeneralSecurityException 
	{
		// type check
		if (!(identity instanceof X509Identity)) {
			return false;
		}
		X509Identity user = (X509Identity) identity;
		
		// pattern check
		if (!matches(user)) {
			return false;
		}
		
		// chains-to-trustroot check
		validateTrust(user);
		
		return true;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
		{
			return false;
		}

		if (!(other instanceof X509PatternAclEntry))
		{
			return false;
		}

		if (!_trustRoot.equals(((X509PatternAclEntry) other)._trustRoot))
		{
			return false;
		}

		if (!_userPattern.equals(((X509PatternAclEntry) other)._userPattern))
		{
			return false;
		}
		

		return true;
	}
	
	public String toString()
	{
		return describe(VerbosityLevel.HIGH);
	}
	
	@Override
	public String describe(VerbosityLevel verbosity)
	{
		if (verbosity.compareTo(VerbosityLevel.HIGH) >= 0)
			return String.format(
					"(X509PatternAclEntry) trustRoot: [%s] userPattern: [%s]", 
					_trustRoot.describe(verbosity),
					X500PrincipalUtilities.describe(
							_userPattern, verbosity));
		else
			return X500PrincipalUtilities.describe(
					_userPattern, 
					VerbosityLevel.HIGH) + 
				" signed by " + _trustRoot.describe(verbosity);
	}

	@Override
	public AclEntry sanitize() {
		return this;
	}	

}
