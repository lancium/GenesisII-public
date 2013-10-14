package edu.virginia.vcgr.genii.algorithm.encryption;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;

import javax.crypto.Cipher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * A class that allows strings to be encrypted and decrypted using a certificate and the
 * corresponding private key.
 * 
 * @author Chris Koeritz
 */
public class SimpleStringEncryptor
{
	static private Log _logger = LogFactory.getLog(SimpleStringEncryptor.class);

	private PrivateKey privKey;
	private X509Certificate cert;
	private BASE64Decoder decoder;
	private BASE64Encoder encoder;
	private Cipher cipher;

	public SimpleStringEncryptor(X509Certificate certificate, PrivateKey privateKey)
	{
		try {
			cipher = Cipher.getInstance("RSA");
		} catch (Throwable e) {
			cipher = null;
			_logger.error("failed to create RSA cipher");
		}

		privKey = (RSAPrivateCrtKey) privateKey;
		cert = certificate;

		decoder = new BASE64Decoder();
		encoder = new BASE64Encoder();
	}

	public String encrypt(String data)
	{
		if (cipher == null) {
			_logger.error("failed to create RSA cipher; cannot encrypt");
			return "";
		}
		try {
			cipher.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
			byte[] encrypted = cipher.doFinal(data.getBytes());
			return encoder.encode(encrypted);
		} catch (Exception err) {
			_logger.error("exception while encrypting: " + err.getMessage());
			return "";
		}
	}

	public String decrypt(String data)
	{
		if (cipher == null) {
			_logger.error("failed to create RSA cipher; cannot decrypt");
			return "";
		}
		try {
			byte[] encrypted = decoder.decodeBuffer(data);
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			byte[] decrypted = cipher.doFinal(encrypted);
			return new String(decrypted);
		} catch (Exception err) {
			_logger.error("exception while decrypting: " + err.getMessage());
			return "";
		}
	}
}
