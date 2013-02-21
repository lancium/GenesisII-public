package edu.virginia.vcgr.genii.client.invoke.handlers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.openssl.PEMWriter;

import sun.misc.BASE64Encoder;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;

public class MyProxyCertificate
{
	private static final ThreadLocal<Boolean> flag = new ThreadLocal<Boolean>();
	private static final ThreadLocal<String> pemFormattedCertificate = new ThreadLocal<String>();
	static private Log _logger = LogFactory.getLog(MyProxyCertificate.class);

	/*
	 * If the user has a myproxy certificate, set a thread local variable to the certificate's pem
	 * formatted String
	 */
	public static void setIfXSEDEUser()
	{
		if (!isPEMFileSetByQueue()) {
			try {
				if (getMyProxyCertificate()) {
					flag.set(true);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}
	}

	public static void setPEMFormattedCertificate(String s)
	{
		flag.set(true);
		pemFormattedCertificate.set(s);
	}

	public static void reset()
	{
		flag.remove();
		pemFormattedCertificate.remove();
	}

	public static boolean isAvailable()
	{
		try {
			return flag.get();
		} catch (NullPointerException e) {
			return false;
		}
	}

	private static boolean isPEMFileSetByQueue()
	{
		return (pemFormattedCertificate == null);
	}

	public static String getPEMString()
	{
		return pemFormattedCertificate.get();
	}

	private static String getCertificateString(PrivateKey privKey, X509Certificate x509Certificate) throws IOException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		new BASE64Encoder().encodeBuffer(privKey.getEncoded(), byteArrayOutputStream);

		StringWriter archive = new StringWriter();

		PEMWriter pemWriter = new PEMWriter(archive);
		pemWriter.writeObject(x509Certificate);
		pemWriter.writeObject(privKey);
		pemWriter.flush();
		pemWriter.close();

		return archive.toString();
	}

	private static boolean getMyProxyCertificate() throws Throwable
	{

		ICallingContext callContext = ContextManager.getCurrentContext(true);
		if (callContext == null)
			return false;

		KeyAndCertMaterial clientKeyMaterial = ClientUtils.checkAndRenewCredentials(callContext, new Date(),
			new SecurityUpdateResults());

		String issuerCommonName = clientKeyMaterial._clientCertChain[0].getIssuerDN().getName();
		if (!issuerCommonName.contains("MyProxy")) {
			return false;
		}

		String certificateString = getCertificateString(clientKeyMaterial._clientPrivateKey,
			clientKeyMaterial._clientCertChain[0]);
		_logger.debug("got certificate for " + clientKeyMaterial._clientCertChain[0].getIssuerDN());
		pemFormattedCertificate.set(certificateString);

		/*
		 * Can be removed after code review. Required to test locally try { BufferedReader reader =
		 * new BufferedReader(new FileReader( "/if8/am2qa/.genesisII-2.0/x509up_u480965")); String
		 * line = null; StringBuilder stringBuilder = new StringBuilder(); String ls =
		 * System.getProperty("line.separator"); while ((line = reader.readLine()) != null) {
		 * stringBuilder.append(line); stringBuilder.append(ls); }
		 * pemFormattedCertificate.set(stringBuilder.toString()); reader.close(); } catch
		 * (IOException e) { e.printStackTrace(); pemFormattedCertificate.set("arbit"); return
		 * false; }
		 */
		return true;
	}
}
