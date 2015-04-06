package edu.virginia.vcgr.genii.security.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.security.cert.CertStore;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class TestSecurityUtilities
{
	static private Log _logger = LogFactory.getLog(TestSecurityUtilities.class);

	/*
	 * future: test is currently tied to the xsede official certs dir. this needs to be fixed for generality.
	 */
	// future: make this into a junit based unit test.

	boolean skipTest = false; // skips all tests if set to true.
	List<X509CRL> foundCrls = null;
	CertStore foundCertStore = null;

	public boolean testSkipper()
	{
		if (skipTest)
			return true;
		File defLoc = new File("/etc/grid-security/certificates");
		if (!defLoc.isDirectory()) {
			skipTest = true;
			return true;
		}
		return false;
	}

	@Test
	public void testLoadCRLsFromDirectory()
	{
		if (testSkipper())
			return;
		if (foundCrls != null)
			return; // already ran.

		File defLoc = new File("/etc/grid-security/certificates");
		List<X509CRL> crls = SecurityUtilities.loadCRLsFromDirectory(defLoc);
		for (X509CRL crl : crls) {
			_logger.info("got a crl for: " + crl.getIssuerDN());
		}
		// shouldn't have failed to load anything.
		assertNotEquals(crls, null);
		// we should have gotten at least one crl if we'll be testing them.
		if (crls != null) {
			if (crls.size() == 0) {
				skipTest = true;
				_logger.warn("no CRLs found in directory, skipping rest of test");
				return;
			}
			foundCrls = crls;
		}
	}

	@Test
	public void testCreateCertStoreFromCRLs()
	{
		if (testSkipper())
			return;
		if (foundCertStore != null)
			return; // already ran.

		// force an ordering where this test runs and loads the found crls.
		testLoadCRLsFromDirectory();
		if (foundCrls != null) {
			CertStore cs = SecurityUtilities.createCertStoreFromCRLs(foundCrls);
			assertNotEquals(cs, null);
			if (cs != null)
				foundCertStore = cs;
		}
	}

	String certExample_3deda549 = "-----BEGIN CERTIFICATE-----\n" + "MIIEDjCCAvagAwIBAgIBADANBgkqhkiG9w0BAQQFADBnMQswCQYDVQQGEwJVUzEN\n"
		+ "MAsGA1UEChMEU0RTQzEQMA4GA1UECxMHU0RTQy1DQTEeMBwGA1UEAxMVQ2VydGlm\n"
		+ "aWNhdGUgQXV0aG9yaXR5MRcwFQYKCZImiZPyLGQBARMHY2VydG1hbjAeFw0wNDA5\n"
		+ "MDkwMjQyMjlaFw0xNDA5MDkwMjQyMjlaMGcxCzAJBgNVBAYTAlVTMQ0wCwYDVQQK\n"
		+ "EwRTRFNDMRAwDgYDVQQLEwdTRFNDLUNBMR4wHAYDVQQDExVDZXJ0aWZpY2F0ZSBB\n"
		+ "dXRob3JpdHkxFzAVBgoJkiaJk/IsZAEBEwdjZXJ0bWFuMIIBIjANBgkqhkiG9w0B\n"
		+ "AQEFAAOCAQ8AMIIBCgKCAQEArXaQKLYiWyAq6ywSOAmKnmiV3u6tXCBmOYlF7Pzp\n"
		+ "hHssQvgomjeh7H31PLzNlxTy/dXQOeyBaDowAL2kCzgwrh/cUo79tZu77Xzgc9o5\n"
		+ "WR+Jq1huI2Au8QEz77PIi2c3fhsWyOYJMvHWXkDXhEr6YxYd1eTaIj435bZOJxVq\n"
		+ "ZF6HePoB5cpflx54KkjjoY3Vh0407EUW9kA7Jcx86dIqH7cSupmPTORsAxvYTmwd\n"
		+ "1qODvd6i06dBcR9VMCTSA4trJTS6pCodKSjLCR6Ru9dgUpwB65gNwH6AVEmmxVet\n"
		+ "oXVsotHTMEghLAp5FBpMNF+s7olt7g19fq8VHnuhRRGkmQIDAQABo4HEMIHBMB0G\n"
		+ "A1UdDgQWBBS/o4cs9g10vUhsDie/AeTyT0a6JzCBkQYDVR0jBIGJMIGGgBS/o4cs\n"
		+ "9g10vUhsDie/AeTyT0a6J6FrpGkwZzELMAkGA1UEBhMCVVMxDTALBgNVBAoTBFNE\n"
		+ "U0MxEDAOBgNVBAsTB1NEU0MtQ0ExHjAcBgNVBAMTFUNlcnRpZmljYXRlIEF1dGhv\n"
		+ "cml0eTEXMBUGCgmSJomT8ixkAQETB2NlcnRtYW6CAQAwDAYDVR0TBAUwAwEB/zAN\n"
		+ "BgkqhkiG9w0BAQQFAAOCAQEAWExIzttzYctC98r6ZC6h2uXPbyo9bvaU0fxtBrDF\n"
		+ "prD23yq8WXClIzKbOxBpRETr1kkeQbzX2R5quFLTTMd6GNqP+I28sklM9FUCLqKV\n"
		+ "DD75UjTqa0AVPgbNdRUECrm2wXXWTVpNIzTgX1M/uVX3yyQRHyi5gj7pqsESOTZ1\n"
		+ "0xyOx4YnpCjrG9HCWIp0wjigWGw8I/GXe0UEPbAJTcPY844Z7E/PfyZuwcdYQSZF\n"
		+ "L030oKjrlWbKm/vGywbqt5QahKM1J60Z6WyIh7VeJV4YuvsP3bT0Sd4FXzmnhybq\n" + "ca98K+/TyIvcWUgD/BGv4XYaUTOYWQRWOZGRSgGfwRq0FQ==\n"
		+ "-----END CERTIFICATE-----";

	@Test
	public void testIsCertRevoked()
	{
		if (testSkipper())
			return;

		// force an ordering where prior test loads certstore.
		testCreateCertStoreFromCRLs();
		if (foundCertStore != null) {
			X509Certificate[] cert = SecurityUtilities.loadCertificateChainFromString(certExample_3deda549);
			// should load at least one cert from that example.
			assertNotEquals(cert.length, 0);
			boolean isRevoked = false;
			try {
				SecurityUtilities.isCertChainRevoked(foundCertStore, cert);
			} catch (Throwable t) {
				isRevoked = true;
			}
			assertFalse(isRevoked);

			InputStream is = TestSecurityUtilities.class.getResourceAsStream("revoked_gw135_iu_xsede_org.cer");
			if (is == null) {
				_logger.warn("test does not run in ant build properly right now; depends on eclipse class path.");
				skipTest = true;
				return;
			}
			X509Certificate[] revCerts = SecurityUtilities.loadCertificateChainFromStream(is);
			assertNotEquals(revCerts.length, 0);

			isRevoked = false; // reset value.
			try {
				SecurityUtilities.isCertChainRevoked(foundCertStore, revCerts);
			} catch (Throwable t) {
				_logger.info("exception received is " + t.getMessage());
				isRevoked = true;
			}
			assertTrue(isRevoked);
		}
	}
}
