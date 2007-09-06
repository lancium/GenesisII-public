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
package edu.virginia.vcgr.genii.client.security.wincrypto;

import java.security.cert.X509Certificate;
import java.util.*;

import java.io.*;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.KeyFactory;
import java.security.cert.CertificateEncodingException;

import edu.virginia.vcgr.genii.client.jni.JNIClientBaseClass;
import edu.virginia.vcgr.genii.client.ser.Base64;

public class WinCryptoLib extends JNIClientBaseClass { 

	//-----------------------------------------------------------------------
	// Native Functions
	//-----------------------------------------------------------------------

	/**
	 * Returns an ArrayList of byte[] arrays, each of which identify a cert
	 * alias in the specified store. Guaranteed to either throw an exception or
	 * return a non-null ArrayList (possibly empty).
	 * 
	 * @param certStore
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private native ArrayList getByteArrayAliases(String certStore)  
			throws WinCryptoException;

	private native RSAPrivateCrtKeySpec getPrivateKeySpec(String certStore,
			byte[] alias) throws WinCryptoException;
	
	private native String getFriendlyName(String certStore,
		byte []alias) throws WinCryptoException;
	
	private native byte[] getCertFromByteAlias(String certStore, byte[] alias)
		throws WinCryptoException;
	
	@SuppressWarnings("unchecked")
	private native ArrayList getCertChain(String certStore, byte[] alias)
		throws WinCryptoException, WinCryptoChainInvalidException;
	
	private native void isCertTrusted(byte[] certBlob)
		throws WinCryptoException, WinCryptoChainInvalidException;

/*	
	public native void MSrsaSignInit(byte[] privatekey, String hashalg);
	public native void MSrsaSignUpdate(byte[] data); 
	public native byte[] MSrsaSign(); 
	public native byte[] MSrsaSignHash(byte[] hash, byte[] privatekey, String hashalg); 
	public native byte[] MSrsaDecrypt(String padalg, byte[] data); 
	public native byte[] MSrsaEncrypt(String padalg, byte[] data); 
	public native int MSrsaGetKeysize();
*/	

	//-----------------------------------------------------------------------
	// Public Interface
	//-----------------------------------------------------------------------
	
	

	/**
	 * Returns an ArrayList of byte[] arrays, each of which identify a cert
	 * alias in the specified store. Guaranteed to either throw an exception or
	 * return a non-null ArrayList (possibly empty).
	 * 
	 * @param certStore
	 * @return The aliases contained within the given certStore.
	 */
	public ArrayList<String> getAliases(String certStore)
			throws WinCryptoException {

		ArrayList<String> retval = new ArrayList<String>();

		Iterator<?> itr = getByteArrayAliases(certStore).iterator();
		while (itr.hasNext()) {
			byte[] byteAlias = (byte[]) itr.next();
			retval.add(Base64.byteArrayToBase64(byteAlias));
		}

		return retval;
	}


	public RSAPrivateCrtKey getPrivateKey(String certStore, String alias)
			throws WinCryptoException {

		// get the keyblob from native code
		RSAPrivateCrtKeySpec keySpec = getPrivateKeySpec(certStore, 
				Base64.base64ToByteArray(alias));
		if (keySpec == null) {
			return null;
		}
		
		try {
			return (RSAPrivateCrtKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new WinCryptoException(e.getMessage(), e);
		} catch (java.security.spec.InvalidKeySpecException e) {
			throw new WinCryptoException(e.getMessage(), e);
		}
	}

	public String getFriendlyName(String certStore, String alias)
		throws WinCryptoException
	{
		return getFriendlyName(certStore, Base64.base64ToByteArray(alias));
	}

	public X509Certificate getCertificate(String certStore, String alias)
			throws WinCryptoException, CertificateException {

		byte[] certblob = getCertFromByteAlias(certStore, Base64.base64ToByteArray(alias));
		
		if (certblob == null) {
			return null;
		}
		
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		ByteArrayInputStream bais = new ByteArrayInputStream(certblob);
		X509Certificate cert = (X509Certificate) cf.generateCertificate(bais);
		try {
			bais.close();
		} catch (Exception e) {
		}
		return cert;
	}

	@SuppressWarnings("unchecked")
	public X509Certificate[] getCertificateChain(String certStore, String alias)
			throws WinCryptoChainInvalidException, WinCryptoException, CertificateException {

		ArrayList<byte[]> certBlobs = getCertChain(certStore, Base64.base64ToByteArray(alias));
		if (certBlobs == null) {
			return null;
		}
		
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		X509Certificate[] chain = new X509Certificate[certBlobs.size()];
		Iterator<byte[]> itr = certBlobs.iterator();
		int i = 0;
		while (itr.hasNext()) {
			byte[] certblob = (byte[]) itr.next();
			ByteArrayInputStream bais = new ByteArrayInputStream(certblob);
			chain[i] = (X509Certificate) cf.generateCertificate(bais);
			try {
				bais.close();
			} catch (Exception e) {
			}
			i++;
		}
		return chain;
	}
	
	public void isCertTrusted(X509Certificate cert) 
			throws WinCryptoException, WinCryptoChainInvalidException, CertificateEncodingException {
		
		isCertTrusted(cert.getEncoded());
		
	}
}
