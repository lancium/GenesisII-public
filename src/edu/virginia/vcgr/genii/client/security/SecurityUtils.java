package edu.virginia.vcgr.genii.client.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import edu.virginia.vcgr.genii.common.security.CertificateChainType;

import org.morgan.util.io.StreamUtils;

public class SecurityUtils
{
	static public final byte[] serializePublicKey(PublicKey pk)
		throws IOException
	{
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		
		try
		{
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(pk);
			oos.flush();
			return baos.toByteArray();
		}
		finally
		{
			StreamUtils.close(oos);
		}
	}
	
	static public final PublicKey deserializePublicKey(byte []data)
		throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		
		try
		{
			bais = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bais);
			return (PublicKey)ois.readObject();
		}
		finally
		{
			StreamUtils.close(ois);
		}
	}
	
	static public final byte[] serializeX509Certificate(X509Certificate cert)
		throws IOException
	{
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;

		try
		{
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(cert);
			oos.flush();
			return baos.toByteArray();
		}
		finally
		{
			StreamUtils.close(oos);
		}
	}

	static public final X509Certificate deserializeX509Certificate(byte []data)
		throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
	
		try
		{
			bais = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bais);
			return (X509Certificate) ois.readObject();
		}
		finally
		{
			StreamUtils.close(ois);
		}	
	}
	
	static public final byte[][] serializeX509CertificateChain(X509Certificate [] certs)
		throws IOException
	{
		byte [][]ret = new byte[certs.length][];
		int lcv = 0;
		for (X509Certificate cert : certs)
		{
			ret[lcv++] = serializeX509Certificate(cert);
		}
	
		return ret;
	}

	static public final X509Certificate [] deserializeX509CertificateChain(byte [][] data)
		throws IOException, ClassNotFoundException
	{
		X509Certificate [] ret = new X509Certificate[data.length];

		for (int i = 0; i < data.length; i++)
		{
			ret[i] = deserializeX509Certificate(data[i]);
		}

		return ret;
	}
	
	static public final X509Certificate [] decodeCertificateChain(CertificateChainType certChain)
		throws GeneralSecurityException
	{
		int numCerts = certChain.getCount();
		X509Certificate [] certs = new X509Certificate[numCerts];

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		for (int i = 0; i < numCerts; i++) {
			byte [] encoded = certChain.getCertificate(i);
			certs[i] = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(encoded));
		}
		return certs;
	}
}