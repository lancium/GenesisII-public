package edu.virginia.vcgr.genii.security.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
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

	// hmmm: test is currently tied to the xsede official certs dir.

	boolean skipTest = false; // skips all tests if set to true.
	List<X509CRL> foundCrls = null;
	CertStore foundCertStore = null;

	public boolean testSkipper()
	{
		if (skipTest) return true;
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
		if (testSkipper()) return;
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
		if (testSkipper()) return;
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

	String certExample_3deda549 = "-----BEGIN CERTIFICATE-----\n"
		+ "MIIEDjCCAvagAwIBAgIBADANBgkqhkiG9w0BAQQFADBnMQswCQYDVQQGEwJVUzEN\n"
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
		+ "L030oKjrlWbKm/vGywbqt5QahKM1J60Z6WyIh7VeJV4YuvsP3bT0Sd4FXzmnhybq\n"
		+ "ca98K+/TyIvcWUgD/BGv4XYaUTOYWQRWOZGRSgGfwRq0FQ==\n" + "-----END CERTIFICATE-----";

	String certExample_verisign = "-----BEGIN CERTIFICATE-----\n"
		+ "MIIGOjCCBSKgAwIBAgIQdwhZFPnLen/JJLhPdVcIyzANBgkqhkiG9w0BAQUFADCB\n"
		+ "vjELMAkGA1UEBhMCVVMxFzAVBgNVBAoTDlZlcmlTaWduLCBJbmMuMR8wHQYDVQQL\n"
		+ "ExZWZXJpU2lnbiBUcnVzdCBOZXR3b3JrMTswOQYDVQQLEzJUZXJtcyBvZiB1c2Ug\n"
		+ "YXQgaHR0cHM6Ly93d3cudmVyaXNpZ24uY29tL3JwYSAoYykwNjE4MDYGA1UEAxMv\n"
		+ "VmVyaVNpZ24gQ2xhc3MgMyBFeHRlbmRlZCBWYWxpZGF0aW9uIFNTTCBTR0MgQ0Ew\n"
		+ "HhcNMTIxMDI5MDAwMDAwWhcNMTQxMDMwMjM1OTU5WjCCAUIxEzARBgsrBgEEAYI3\n"
		+ "PAIBAxMCVVMxGTAXBgsrBgEEAYI3PAIBAhMIRGVsYXdhcmUxHTAbBgNVBA8TFFBy\n"
		+ "aXZhdGUgT3JnYW5pemF0aW9uMRAwDgYDVQQFEwcyMTU4MTEzMQswCQYDVQQGEwJV\n"
		+ "UzEOMAwGA1UEERQFOTQwNDMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcU\n"
		+ "DU1vdW50YWluIFZpZXcxGTAXBgNVBAkUEDM1MCBFbGxpcyBTdHJlZXQxHTAbBgNV\n"
		+ "BAoUFFN5bWFudGVjIENvcnBvcmF0aW9uMTkwNwYDVQQLFDBJbmZyYXN0cnVjdHVy\n"
		+ "ZSBPcGVyYXRpb25zIFN5bWFudGVjIFNTUEVWIFJldm9rZWQxIDAeBgNVBAMUF3Rl\n"
		+ "c3Qtc3NwZXYudmVyaXNpZ24uY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n"
		+ "CgKCAQEAyA+FiyGlE4DgFjBNuttRmss2PLDgDYELdd4+i2e38i3t7RoX4xbZAUxn\n"
		+ "95iIpWac95MBd+TB1lE1sXap7vpEVamcDkvCfr6Xc9sg/idmE1FXMC0ztwscuhHh\n"
		+ "6AMYDezqMFibPmmiQjqTaDso/jsOXlQ8QiVPIswVV9fSq7rcus4zPpdORp9qPv/1\n"
		+ "jIoAcEY5jIfLGKoBZdCpEPP3nW5yFkfmhPc6WCogjlRGowoJdmZWXn+sYZ4RiH1P\n"
		+ "lwzPdZ8n7173IIJ7SI0ChudHVtd0xjryqr/eiOnds0BLf7KVEAj/wVIVZc7wtN1q\n"
		+ "q7GugVZw7jrEIX8oN5oOmY6bcZ6v8wIDAQABo4IBqzCCAacwIgYDVR0RBBswGYIX\n"
		+ "dGVzdC1zc3Bldi52ZXJpc2lnbi5jb20wCQYDVR0TBAIwADAdBgNVHQ4EFgQUgAiP\n"
		+ "NfJxyo5a+7S8XNAwa5soLTswDgYDVR0PAQH/BAQDAgWgMD4GA1UdHwQ3MDUwM6Ax\n"
		+ "oC+GLWh0dHA6Ly9FVkludGwtY3JsLnZlcmlzaWduLmNvbS9FVkludGwyMDA2LmNy\n"
		+ "bDBEBgNVHSAEPTA7MDkGC2CGSAGG+EUBBxcGMCowKAYIKwYBBQUHAgEWHGh0dHBz\n"
		+ "Oi8vd3d3LnZlcmlzaWduLmNvbS9jcHMwKAYDVR0lBCEwHwYIKwYBBQUHAwEGCCsG\n"
		+ "AQUFBwMCBglghkgBhvhCBAEwHwYDVR0jBBgwFoAUTkPIHXbvN1N6T/JYb5TzOOLV\n"
		+ "vd8wdgYIKwYBBQUHAQEEajBoMCsGCCsGAQUFBzABhh9odHRwOi8vRVZJbnRsLW9j\n"
		+ "c3AudmVyaXNpZ24uY29tMDkGCCsGAQUFBzAChi1odHRwOi8vRVZJbnRsLWFpYS52\n"
		+ "ZXJpc2lnbi5jb20vRVZJbnRsMjAwNi5jZXIwDQYJKoZIhvcNAQEFBQADggEBAC+7\n"
		+ "zcF1013Rm5ZTificyYyoT7Z5I4kC3s0IIbEh+4eau4kjZ9kPcHqR9tJdlqHyjbTy\n"
		+ "+YlFr9JaV6gIF6K08Vp4+zLX2uLvZ2d8zG7GtFfLl9qRazKcnhZqU4aVk05kAsV7\n"
		+ "m8hFY0MLphbwQ5TRHYGQJevdO1/fJDcWxh7Z7dcXDCLlknxfMYM9PfYcgO+dLHsq\n"
		+ "tO3LWRfQ/2e9zJZ5EPI8+pJgUllPtsM6u69ZjPK9oU1ppkGqXMmnbSQbdOi2YziC\n"
		+ "EYJsDPHhyankvKfFY83Pg4BdpiGuAUM7/xQhNv07mdUmxYz3XmDjYRxTz7Fk4Cye\n" + "2I/KqPh2fRX6wOpqbnw=\n"
		+ "-----END CERTIFICATE-----";

	@Test
	public void testIsCertRevoked()
	{
		if (testSkipper()) return;
		
		// force an ordering where prior test loads certstore.
		testCreateCertStoreFromCRLs();
		if (foundCertStore != null) {
			X509Certificate cert = SecurityUtilities.loadCertificateFromString(certExample_3deda549);
			assertFalse(SecurityUtilities.isCertRevoked(foundCertStore, cert));

			// not catching as revoked. do we need more of the certs in chain? or is verisign not even in list?
			// X509Certificate cert2 = SecurityUtilities.loadCertificateFromString(certExample_verisign);
			// assertTrue(SecurityUtilities.isCertRevoked(foundCertStore, cert2));
		}

	}

}
