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
package edu.virginia.vcgr.genii.client.security.wincrypto.test;

import java.util.*;
import java.io.*;

import java.security.cert.X509Certificate;
import java.security.*;
import java.security.interfaces.*;

import javax.crypto.Cipher;

import javax.crypto.*;
import javax.crypto.spec.*;

import edu.virginia.vcgr.genii.client.security.wincrypto.*;


public class WinCryptoLibTest {

	/**
	 * Initializes an object output stream that encrypts using a freshly
	 * generated 3DES session key. This session key is wrapped using the
	 * supplied asymmetric key and placed at the beginning of the output
	 * 
	 * @param asymmetricKey
	 * @param baseOutput
	 * @return
	 * @throws Exception
	 */
	public static OutputStream initCipherOutputStream(Key asymmetricKey,
			OutputStream baseOutput) throws Exception {
		// Generate a triple-DES key
		KeyGenerator keygen = KeyGenerator.getInstance("DESede");
		keygen.init(new SecureRandom());
		Key desSessionKey = keygen.generateKey();

		// create cipher for wrapping DESede KEY and wrap (encrypt) it
		Cipher wrapCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		wrapCipher.init(Cipher.WRAP_MODE, asymmetricKey);
		byte[] wrappedDesKey = wrapCipher.wrap(desSessionKey);

		// write the wrappedDesKey directly to the stream
		baseOutput.write(wrappedDesKey.length);
		baseOutput.write(wrappedDesKey);
		baseOutput.flush();

		// Create the DESede session cipher (stream mode), get its
		// initialization vector (iv)
		Cipher desEncryptCipher = Cipher.getInstance("DESede/CFB8/NoPadding");
		desEncryptCipher.init(Cipher.ENCRYPT_MODE, desSessionKey);
		byte[] iv = desEncryptCipher.getIV();

		// write the iv directly to the stream
		// write the wrappedDesKey directly to the stream
		baseOutput.write(iv.length);
		baseOutput.write(iv);
		baseOutput.flush();

		// Create the DESede enciphering outputstream
		return new CipherOutputStream(baseOutput, desEncryptCipher);

	}

	/**
	 * Initializes an object input stream that decrypts input from the base
	 * input. Decryption is done with a session key that is unwrapped from the
	 * beginning of the stream with the supplied assymetric key
	 * 
	 * @param asymmetricKey
	 * @param baseInput
	 * @return
	 * @throws Exception
	 */
	public static InputStream initCipherInputStream(Key asymmetricKey,
			InputStream baseInput) throws Exception {
		// read the wrapped session key
		int amt = baseInput.read();
		byte[] recvWrappedSessionKey = new byte[amt];
		while (amt > 0) {
			amt -= baseInput.read(recvWrappedSessionKey,
					recvWrappedSessionKey.length - amt, amt);
		}

		// create RSA unswrap cipher and decrypt session key
		Cipher unwrapCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		unwrapCipher.init(Cipher.UNWRAP_MODE, asymmetricKey);
		Key desSessionKey = unwrapCipher.unwrap(recvWrappedSessionKey,
				"DESede", Cipher.SECRET_KEY);

		// read the initialization vector (iv)
		amt = baseInput.read();
		byte[] iv = new byte[amt];
		while (amt > 0) {
			amt -= baseInput.read(iv, iv.length - amt, amt);
		}

		// create the DESede decription cipher
		Cipher desDecryptCipher = Cipher.getInstance("DESede/CFB8/NoPadding");
		desDecryptCipher.init(Cipher.DECRYPT_MODE, desSessionKey,
				new IvParameterSpec(iv));

		// Create the DESede deciphering inputstream
		return new CipherInputStream(baseInput, desDecryptCipher);
	}

	/**
	 * @param args
	 */
	public static void test(PrivateKey privKey, PublicKey pubKey)
			throws Exception {

		final String MESSAGETEXT = "The quick brown dog jumps over the lazy fox.";
		
		// create a dummy output stream
		ByteArrayOutputStream dummyOutput = new ByteArrayOutputStream();
		
		// create an encryption stream that encrypts with the
		// recipient's public key, wrap it with a stream that signs
		// it with our private key
		ObjectOutputStream cipherOutput = new ObjectOutputStream(
				initCipherOutputStream(
						privKey,
						initCipherOutputStream(pubKey, dummyOutput)));

		// write a message to the stream
		System.out.println("Writing message...");
		cipherOutput.writeUTF(MESSAGETEXT);
		cipherOutput.flush();

		// get the encrypted bytes
		byte[] cipherText = dummyOutput.toByteArray();
		System.out.println("\nEncrypted text: " + new String(cipherText) + "\n");

		// create a dummy input stream
		ByteArrayInputStream dummyInput = new ByteArrayInputStream(cipherText);

		// create a decryption stream, wrap it with a stream that
		// verifies the sender's identity
		ObjectInputStream cipherIn = new ObjectInputStream(
				initCipherInputStream(pubKey,
						initCipherInputStream(privKey, dummyInput)));

		// read the message from the stream
		String receivedMessage = cipherIn.readUTF();
		System.out.println("Got: " + receivedMessage);
		
		if (!receivedMessage.equals(MESSAGETEXT)) {
			System.out.println("DECRYPTION FAILED!");
		}
	}

	/**
	 * Iterates through the personal certificates, looking for a 
	 * public/private keypair to test encryption with
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		WinCryptoLib cryptoLib = new WinCryptoLib();

		String stores[] = {"My"};
//		String stores[] = {"CA", "Root", "My"};
		
		for (int s = 0; s < stores.length; s++) {
		
			ArrayList<String> aliases = cryptoLib.getAliases(stores[s]);
			Iterator<String> itr = aliases.iterator();
			while (itr.hasNext()) {
				String alias = itr.next();
	
				X509Certificate cert = cryptoLib.getCertificate(stores[s], alias);
				
				System.out.println(cert.getClass().getName());
				
				if (cert != null) {
					System.out.println("CERT:\n"
						+ cert.getSubjectX500Principal().getName());
				}
	
				PublicKey publicKey = cert.getPublicKey();
				System.out.println("Public key: " + publicKey);
	
				RSAPrivateCrtKey privateKey = cryptoLib.getPrivateKey(stores[s], alias);
				if (privateKey != null) {
					System.out.println("Private key: " + privateKey);
					test(privateKey, publicKey);
				}
	
				try {
					X509Certificate[] chain = cryptoLib.getCertificateChain(stores[s], alias);
					if (chain != null) {
						System.out.println("Cert chain:");
						for (int i = 0; i < chain.length; i++) {
							System.out.println(i + ": " + chain[i].getSubjectX500Principal().getName());
						}
					}
				} catch (WinCryptoChainInvalidException e) {
					System.err.println("Cert chain invalid: " + e.getMessage());
					
					// double check failure
					try {
						cryptoLib.isCertTrusted(cert);
						System.out.println("INCONSISTENT!!");
					} catch (Exception ex) {
						System.err.println("CONFIRM: " + e.getMessage());
					}
					
				}
				
				System.out.println();
			}
		}
		
		System.out.println("Key Manager output:");
		
		WinX509KeyManager km = new WinX509KeyManager();
		String[] a = km.getClientAliases(null, null);
		for (int i = 0; i < a.length; i++) {
			System.out.println("km alias: " + ((X509Certificate) km.getCertificateChain(a[i])[0]).getSubjectX500Principal().getName());
			if (km.getPrivateKey(a[i]) != null) {
				System.out.println("Contains exportable private key");
			}
		}
	}
}
