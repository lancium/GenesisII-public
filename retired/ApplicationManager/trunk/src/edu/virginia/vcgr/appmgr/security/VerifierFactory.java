package edu.virginia.vcgr.appmgr.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.LinkedList;

import edu.virginia.vcgr.appmgr.io.IOUtils;

public class VerifierFactory
{
	static private class CertificateVerifier implements Verifier
	{
		private Certificate[] _allowedCertificates;

		private CertificateVerifier(Certificate... allowedCertificates)
		{
			_allowedCertificates = allowedCertificates;
		}

		@Override
		public void verify(String entryName, Object... signers) throws VerificationException
		{
			if (signers == null)
				throw new VerificationException(String.format("Entry \"%s\" does not appear to be signed.", entryName));

			for (Object signer : signers) {
				if (signer instanceof Certificate) {
					for (Certificate cert : _allowedCertificates) {
						if (cert.equals(signer))
							return;
					}
				}
			}

			throw new VerificationException(String.format("Unable to verify entry \"%s\".", entryName));
		}
	}

	static public Verifier createCertificateVerifier(Certificate... certificates)
	{
		return new CertificateVerifier(certificates);
	}

	static public Verifier createCertificateVerifier(File... certificateFiles) throws IOException, CertificateException
	{
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		Collection<Certificate> certificates = new LinkedList<Certificate>();
		InputStream in;

		for (File certificateFile : certificateFiles) {
			in = null;

			try {
				in = new FileInputStream(certificateFile);
				certificates.add(certFactory.generateCertificate(in));
			} finally {
				IOUtils.close(in);
			}
		}

		return createCertificateVerifier(certificates.toArray(new Certificate[0]));
	}
}