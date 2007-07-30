/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.morgan.util.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.morgan.util.cmdline.CommandLine;
import org.morgan.util.cmdline.CommandLineFlavor;
import org.morgan.util.io.StreamUtils;


/**
 * A simple utility for helping with the signing of files.
 *
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class SigningHelper
{
	static public byte[] signStream(InputStream in, PrivateKey k)
		throws NoSuchAlgorithmException, InvalidKeyException, IOException,
		SignatureException
	{
		Signature signer = Signature.getInstance(k.getAlgorithm());
		signer.initSign(k);
		
		byte []buffer = new byte[8192];
		int read;
		while ( (read = in.read(buffer)) != -1)
			signer.update(buffer, 0, read);
		
		return signer.sign();
	}
	
	static public byte[] signStream(InputStream in, String keystoreAlias,
		String keystorePassword) 
			throws KeyStoreException, UnrecoverableKeyException,
				NoSuchAlgorithmException, IOException, InvalidKeyException,
				SignatureException, CertificateException
	{
		FileInputStream fin = null;
		KeyStore store = KeyStore.getInstance("JKS");
		try
		{
			fin = new FileInputStream(
				System.getProperty("user.home") + "/.keystore");
			store.load(fin, keystorePassword.toCharArray());
		}
		finally
		{
			StreamUtils.close(fin);
		}
		return signStream(in, (PrivateKey)store.getKey(keystoreAlias,
			keystorePassword.toCharArray()));
	}
	
	static public void signFile(File input, File signatureFile,
		String keystoreAlias, String keystorePassword)
			throws KeyStoreException, UnrecoverableKeyException,
				NoSuchAlgorithmException, IOException, InvalidKeyException,
				SignatureException, CertificateException
	{
		FileInputStream fin = null;
		FileOutputStream fout = null;
		
		try
		{
			fin = new FileInputStream(input);
			byte []signature = signStream(fin, keystoreAlias, keystorePassword);
			fout = new FileOutputStream(signatureFile);
			fout.write(signature);
		}
		finally
		{
			StreamUtils.close(fin);
			StreamUtils.close(fout);
		}
	}
	
	static public void signFile(File input, File signatureFile, PrivateKey key)
		throws KeyStoreException, UnrecoverableKeyException,
			NoSuchAlgorithmException, IOException, InvalidKeyException,
			SignatureException
	{
		FileInputStream fin = null;
		FileOutputStream fout = null;
		
		try
		{
			fin = new FileInputStream(input);
			byte []signature = signStream(fin, key);
			fout = new FileOutputStream(signatureFile);
			fout.write(signature);
		}
		finally
		{
			StreamUtils.close(fin);
			StreamUtils.close(fout);
		}
	}
	
	static public boolean verifySignature(InputStream in, byte []signature,
		Certificate cert) 
			throws SignatureException, IOException, NoSuchAlgorithmException,
				InvalidKeyException
	{
		PublicKey key = cert.getPublicKey();
		Signature signer = Signature.getInstance(key.getAlgorithm());
		signer.initVerify(cert);
		
		byte []buffer = new byte[8192];
		int read;
		while ( (read = in.read(buffer)) != -1)
			signer.update(buffer, 0, read);
		
		return signer.verify(signature);
	}
	
	static public boolean verifySignature(InputStream in, byte []signature,
		String keystoreAlias, String keyStorepassword)
		throws SignatureException, IOException, 
			NoSuchAlgorithmException, KeyStoreException, CertificateException,
			InvalidKeyException
	{
		FileInputStream fin = null;
		
		KeyStore store = KeyStore.getInstance("JKS");
		try
		{
			fin = new FileInputStream(
				System.getProperty("user.home") + "/.keystore");
			store.load(fin, keyStorepassword.toCharArray());
		}
		finally
		{
			StreamUtils.close(fin);
		}
		return verifySignature(in, signature, 
			store.getCertificate(keystoreAlias));
	}
	
	static private byte[] readSignature(File signatureFile)
		throws IOException
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(signatureFile);
			byte []ret = new byte[fin.available()];
			fin.read(ret);
			return ret;
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	static public boolean verifySignature(File inputFile, File signatureFile,
		String keystoreAlias, String keystorePassword)
		throws SignatureException, IOException, 
			NoSuchAlgorithmException, KeyStoreException, CertificateException,
			InvalidKeyException
	{
		FileInputStream fin = null;
		
		byte []signature = readSignature(signatureFile);
		try
		{
			fin = new FileInputStream(inputFile);
			return verifySignature(fin, signature, 
				keystoreAlias, keystorePassword);
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	static public boolean verifySignature(File inputFile, File signatureFile,
		Certificate cert)
			throws SignatureException, IOException, NoSuchAlgorithmException,
				InvalidKeyException
	{
		FileInputStream fin = null;
		
		byte []signature = readSignature(signatureFile);
		try
		{
			fin = new FileInputStream(inputFile);
			return verifySignature(fin, signature, cert);
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	static private void usage()
	{
		System.err.println("USAGE:  SigningHelper --sign <keystore-alias> <keystore-password> <input-file> <signature-file>");
		System.err.println("\t\tOR");
		System.err.println("\tSigningHelper --verify <keystore-alias> <keystore-password> <input-file> <signature-file>");
		System.err.println("\t\tOR");
		System.err.println("\tSigningHelper --verify <cert-file> <input-file> <signature-file>");
	}
	
	static private void sign(String alias, String password,
		String inputFile, String outputFile) throws Exception
	{
		signFile(new File(inputFile), new File(outputFile), alias, password);
	}
	
	static private void verify(String alias, String password,
		String inputFile, String signatureFile) throws Exception
	{
		if (verifySignature(new File(inputFile), new File(signatureFile),
			alias, password))
		{
			System.out.println("Verified");
		} else
		{
			System.out.println("Not Verified.");
		}
	}
	
	static private void verify(String certFile, 
		String inputFile, String signatureFile) throws Exception
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(certFile);
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			Certificate cert = factory.generateCertificate(fin);
			if (verifySignature(new File(inputFile), new File(signatureFile),
				cert))
			{
				System.out.println("Verified");
			} else
			{
				System.out.println("Not Verified.");
			}
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	static public void main(String []args) throws Exception
	{
		CommandLineFlavor signFlavor = new CommandLineFlavor("sign", 4);
		signFlavor.addRequiredFlag("sign");
		CommandLineFlavor verifyStoreFlavor = new CommandLineFlavor("verify", 4);
		verifyStoreFlavor.addRequiredFlag("verify");
		CommandLineFlavor verifyCertFlavor = new CommandLineFlavor("verify", 3);
		
		CommandLine cLine = new CommandLine(args);
		
		if (signFlavor.matches(cLine))
			sign(cLine.getArgument(0), cLine.getArgument(1), 
				cLine.getArgument(2), cLine.getArgument(3));
		else if (verifyStoreFlavor.matches(cLine))
			verify(cLine.getArgument(0), cLine.getArgument(1), 
				cLine.getArgument(2), cLine.getArgument(3));
		else if (verifyCertFlavor.matches(cLine))
			verify(cLine.getArgument(0), cLine.getArgument(1), 
				cLine.getArgument(2));
		else
		{
			usage();
			System.exit(1);
		}
	}
}
