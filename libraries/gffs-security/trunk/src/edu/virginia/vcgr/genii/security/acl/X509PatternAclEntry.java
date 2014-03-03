package edu.virginia.vcgr.genii.security.acl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Vector;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;

import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.X500PrincipalUtilities;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.Identity;
import edu.virginia.vcgr.genii.security.utils.SecurityUtilities;
import eu.emi.security.authn.x509.CommonX509TrustManager;
import eu.emi.security.authn.x509.impl.InMemoryKeystoreCertChainValidator;

/**
 * This ACL rule provides a chain of trust that callers must chain to, and,
 * optionally, a pattern that the caller's principal name must match.
 * 
 * The pattern allows for a spectrum of principal-grouping possibilities, e.g.:
 * - A null pattern allows for grouping of X.509 principals by the specified
 * certification authority - A partial pattern allows for grouping of
 * logically-equivalent principals (i.e., to allow for certificates in which the
 * distinguished name may vary slightly between versions, e.g., proxy
 * certificates)
 * 
 * @author dgm4d
 */
@SuppressWarnings("deprecation")
public class X509PatternAclEntry implements AclEntry {
	static public final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(X509PatternAclEntry.class);

	protected X509Identity _trustRoot;
	protected X500Principal _userPattern;

	// cache a trust manager upon first use.
	transient protected CommonX509TrustManager _trustManagerCanl;
	transient protected X509TrustManager _trustManagerJdk;

	// cache an X509 pattern upon first use.
	transient protected X509Principal _bcPattern;

	public X509PatternAclEntry(X509Identity trustRoot, X500Principal userPattern) {
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

	synchronized void initTrustManager() throws GeneralSecurityException,
			IOException {
		if (_trustManagerJdk == null) {
			// create an in-memory cert keystore for the trusted certs.
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(null, null);

			// add the trusted cert into the memory-keystore.
			X509Certificate trustedCert = _trustRoot.getOriginalAsserter()[0];
			ks.setCertificateEntry(trustedCert.getSubjectX500Principal()
					.getName(), trustedCert);

			// create a trust manager from the key store.
			PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(ks,
					new X509CertSelector());
			pkixParams.setRevocationEnabled(false);
			ManagerFactoryParameters trustParams = new CertPathTrustManagerParameters(
					pkixParams);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
			tmf.init(trustParams);
			_trustManagerJdk = (X509TrustManager) tmf.getTrustManagers()[0];
		}
		if (_trustManagerCanl == null) {
			// create an in-memory cert keystore for the trusted certs.
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(null, null);

			// add the trusted cert into the memory-keystore.
			X509Certificate trustedCert = _trustRoot.getOriginalAsserter()[0];
			ks.setCertificateEntry(trustedCert.getSubjectX500Principal()
					.getName(), trustedCert);

			InMemoryKeystoreCertChainValidator validater = new InMemoryKeystoreCertChainValidator(
					ks);
			_trustManagerCanl = new CommonX509TrustManager(validater);
		}
	}

	protected boolean validateTrust(X509Identity user) {
		try {
			initTrustManager();
		} catch (Throwable e) {
			_logger.error(
					"exception when initializing trust manager: "
							+ e.getMessage(), e);
			return false;
		}
		boolean trustOkay = false;
		X509Certificate[] userCertChain = user.getOriginalAsserter();
		try {
			_trustManagerCanl.checkClientTrusted(userCertChain,
					userCertChain[0].getPublicKey().getAlgorithm());
			trustOkay = true;
		} catch (Throwable e) {
			if (_logger.isDebugEnabled())
				_logger.debug("problem checking cert with canl: "
						+ e.getMessage());
		}
		try {
			if (!trustOkay) {
				_trustManagerJdk.checkClientTrusted(userCertChain,
						userCertChain[0].getPublicKey().getAlgorithm());
				trustOkay = true;
			}
		} catch (Throwable e) {
			if (_logger.isDebugEnabled())
				_logger.debug("problem checking cert with jdk: "
						+ e.getMessage());
		}

		if (!trustOkay) {
			if (_logger.isDebugEnabled())
				_logger.debug("client not trusted by pattern acl, user[1 of "
						+ userCertChain.length
						+ "] chksum="
						+ SecurityUtilities.getChecksum(userCertChain[0])
						+ " pubkey chksum="
						+ SecurityUtilities.getChecksum(userCertChain[0]
								.getPublicKey()) + " is "
						+ userCertChain[0].toString());
		} else {
			if (_logger.isDebugEnabled())
				_logger.debug("trust established by pattern acl for user[1 of "
						+ userCertChain.length
						+ "] chksum="
						+ SecurityUtilities.getChecksum(userCertChain[0])
						+ " pubkey chksum="
						+ SecurityUtilities.getChecksum(userCertChain[0]
								.getPublicKey()) + " is "
						+ userCertChain[0].toString());
		}
		return trustOkay;
	}

	@SuppressWarnings("unchecked")
	protected boolean matches(X509Identity user) {
		if (_userPattern == null) {
			return true;
		}

		X509Certificate userCert = user.getOriginalAsserter()[0];
		X509Principal userPrincipal;
		try {
			userPrincipal = PrincipalUtil.getSubjectX509Principal(userCert);
		} catch (CertificateEncodingException t) {
			_logger.debug(
					"cert encoding problem for: "
							+ userCert.getSubjectX500Principal().getName()
							+ " -- " + t.getMessage(), t);
			return false;
		}

		// attempt to match each pattern with one in the caller's DN.
		try {
			initPattern();
		} catch (GeneralSecurityException t) {
			_logger.debug(
					"pattern initialization problem for: "
							+ userCert.getSubjectX500Principal().getName()
							+ " -- " + t.getMessage(), t);
			return false;
		}
		Vector<ASN1ObjectIdentifier> oids = _bcPattern.getOIDs();
		for (ASN1ObjectIdentifier oid : oids) {
			Vector<String> patternValues = _bcPattern.getValues(oid);
			Vector<String> userValues = userPrincipal.getValues(oid);
			if ((userValues == null)
					|| (!userValues.containsAll(patternValues))) {
				if (_logger.isTraceEnabled())
					_logger.trace("failed to match object identifiers for: "
							+ userCert.getSubjectX500Principal().getName());
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isPermitted(Identity identity)
			throws GeneralSecurityException {
		// type check
		if (!(identity instanceof X509Identity)) {
			return false;
		}
		X509Identity user = (X509Identity) identity;
		String userDN = user.getOriginalAsserter()[0].getSubjectX500Principal()
				.getName().toString();

		// pattern check
		if (!matches(user)) {
			if (_logger.isTraceEnabled())
				_logger.trace("user " + userDN + " does not match pattern: "
						+ _userPattern);
			return false;
		}
		if (_logger.isTraceEnabled())
			_logger.trace("user " + userDN + " matches pattern: "
					+ _userPattern);
		return validateTrust(user);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}

		if (!(other instanceof X509PatternAclEntry)) {
			return false;
		}

		if (!_trustRoot.equals(((X509PatternAclEntry) other)._trustRoot)) {
			return false;
		}

		if (!_userPattern.equals(((X509PatternAclEntry) other)._userPattern)) {
			return false;
		}

		return true;
	}

	public String toString() {
		return describe(VerbosityLevel.HIGH);
	}

	@Override
	public String describe(VerbosityLevel verbosity) {
		if (verbosity.compareTo(VerbosityLevel.HIGH) >= 0)
			return String.format(
					"(X509PatternAclEntry) trustRoot: [%s] userPattern: [%s]",
					_trustRoot.describe(verbosity),
					X500PrincipalUtilities.describe(_userPattern, verbosity));
		else
			return X500PrincipalUtilities.describe(_userPattern,
					VerbosityLevel.HIGH)
					+ " signed by "
					+ _trustRoot.describe(verbosity);
	}

	@Override
	public AclEntry sanitize() {
		return this;
	}
}
