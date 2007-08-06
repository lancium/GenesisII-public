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
package edu.virginia.vcgr.genii.client.security.x509;

import java.security.cert.*;

import java.security.*;
import java.math.*;
import java.util.*;
import java.io.*;

import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.jce.provider.*;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.*;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.bouncycastle.asn1.x509.X509Name; 

import java.util.Random;


/**
 * A simple example that generates an attribute certificate.
 */
public class CertTool {

	static boolean loaded = false;

    static Random serialNumRandomness = new Random(System.currentTimeMillis());

    static final int SERIAL_NUM_BITS = 128;
	
    static {
    	loadBCProvider();
    }
    
    /**
	 * Setup the bouncy castle provider
	 */
	public static void loadBCProvider() {
		synchronized (CertTool.class) {
			if (!loaded) {
				Security.addProvider(new BouncyCastleProvider());
				loaded = true;
			}
		}
	}

    
    /**
     * we generate the CA's certificate
     */
    public static X509Certificate createMasterCert(
		String dn, 
		long validityMillis,
        PublicKey       pubKey,
        PrivateKey      privKey)
        throws GeneralSecurityException
    {
        String  issuer = dn;
        String  subject = dn;

        //
        // create the certificate - version 1
        //
        X509V1CertificateGenerator  v1CertGen = new X509V1CertificateGenerator();

        v1CertGen.setSerialNumber(new BigInteger(SERIAL_NUM_BITS, serialNumRandomness));
        v1CertGen.setIssuerDN(new X509Principal(issuer));
        v1CertGen.setNotBefore( // 15 minutes ago
				new Date(System.currentTimeMillis() - (1000L * 60 * 15)));
        v1CertGen.setNotAfter(new Date(System.currentTimeMillis()
				+ validityMillis));
        v1CertGen.setSubjectDN(new X509Principal(subject));
        v1CertGen.setPublicKey(pubKey);
        v1CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        X509Certificate cert = v1CertGen.generateX509Certificate(privKey);

        cert.checkValidity(new Date());

        cert.verify(pubKey);

        PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)cert;

        //
        // this is actually optional - but if you want to have control
        // over setting the friendly name this is the way to do it...
        //
        bagAttr.setBagAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
            new DERBMPString(getCN(cert)));

        return cert;
    }

    /**
     * we generate an intermediate certificate signed by our CA
     */
    public static X509Certificate createIntermediateCert(
		String dn, 
		long validityMillis,
        PublicKey       pubKey,
        PrivateKey      caPrivKey,
        X509Certificate caCert)
        throws GeneralSecurityException
    {

        //
        // create the certificate - version 3
        //
        X509V3CertificateGenerator  v3CertGen = new X509V3CertificateGenerator();

        v3CertGen.setSerialNumber(new BigInteger(SERIAL_NUM_BITS, serialNumRandomness));
        v3CertGen.setIssuerDN(PrincipalUtil.getSubjectX509Principal(caCert));
        v3CertGen.setNotBefore( // 24 hours ago
				new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 24)));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis()
				+ validityMillis));
        v3CertGen.setSubjectDN(new X509Principal(dn));
        v3CertGen.setPublicKey(pubKey);
        v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        //
        // extensions
        //
        v3CertGen.addExtension(
            X509Extensions.SubjectKeyIdentifier,
            false,
            new SubjectKeyIdentifierStructure(pubKey));
        v3CertGen.addExtension(
            X509Extensions.AuthorityKeyIdentifier,
            false,
            new AuthorityKeyIdentifierStructure(caCert));
        v3CertGen.addExtension(
            X509Extensions.BasicConstraints,
            true,
            new BasicConstraints(true));
        X509Certificate cert = v3CertGen.generateX509Certificate(caPrivKey);

        cert.checkValidity(new Date());
        cert.verify(caCert.getPublicKey());
        PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)cert;

        //
        // this is actually optional - but if you want to have control
        // over setting the friendly name this is the way to do it...
        //
        bagAttr.setBagAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
            new DERBMPString(getCN(cert)));
        return cert;
    }	

	@SuppressWarnings("unchecked")
	public static String getUID(X509Certificate cert) {
		X509Name dn = new X509Name(cert.getSubjectDN().toString());
		Vector<DERObjectIdentifier> oids = dn.getOIDs();
		Vector<String> values = dn.getValues();
		Iterator<DERObjectIdentifier> oidItr = oids.iterator();
		Iterator<String> valItr = values.listIterator();
		while (oidItr.hasNext()) {
        	DERObjectIdentifier oid = oidItr.next();
        	String value = valItr.next();
        	if (oid.equals(X509Name.UID)) {
        		return value;
        	}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static String getCN(X509Certificate cert) {
		X509Name dn = new X509Name(cert.getSubjectDN().toString());
		Vector<DERObjectIdentifier> oids = dn.getOIDs();
		Vector<String> values = dn.getValues();
		Iterator<DERObjectIdentifier> oidItr = oids.iterator();
		Iterator<String> valItr = values.listIterator();
		while (oidItr.hasNext()) {
        	DERObjectIdentifier oid = oidItr.next();
        	String value = valItr.next();
        	if (oid.equals(X509Name.CN)) {
        		return value;
        	}
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public static X509Certificate[] createResourceCertChain(
			String epi, 
			String newCN, 
			CertCreationSpec certSpec) throws GeneralSecurityException {
		
		// replace the UID and the CN, if necessary
		X509Name dn = new X509Name(certSpec.issuerChain[0].getSubjectDN().toString());
		Vector<DERObjectIdentifier> oids = dn.getOIDs();
		Vector<String> values = dn.getValues();
		Iterator<DERObjectIdentifier> oidItr = oids.iterator();
		ListIterator<String> valItr = values.listIterator();
		boolean uidSet = false;
		while (oidItr.hasNext()) {
        	DERObjectIdentifier oid = oidItr.next();
        	valItr.next();
        	if (oid.equals(X509Name.UID)) {
        		valItr.set(epi);
        		uidSet = true;
        	} else if ((newCN != null) && (oid.equals(X509Name.CN))) {
        		valItr.set(newCN);
        	}
		}
		if (!uidSet) {
			oids.add(X509Name.UID);
			values.add(epi);
		}
		dn = new X509Name(oids, values);
		
		X509Certificate newCert = CertTool.createIntermediateCert(
				dn.toString(), 
				certSpec.validityMillis, 
				certSpec.newPublicKey, 
				certSpec.issuerPrivateKey, 
				certSpec.issuerChain[0]); 
		X509Certificate[] newCertChain = new X509Certificate[certSpec.issuerChain.length + 1];
		newCertChain[0] = newCert;
		for (int i = 0; i < certSpec.issuerChain.length; i++) {
			newCertChain[i + 1] = certSpec.issuerChain[i];
		}
		return newCertChain;
		
	}
	
	/**
	 * Loads a keystore
	 * 
	 * @param location
	 * @param type
	 * @param password
	 * @return Returns the keystore loaded from the given location.
	 */
	public static KeyStore openStoreDirectPath(String location, String type, 
			char[] password) throws GeneralSecurityException, IOException  {
		// try both providers (BC and the default SunJCE)
		KeyStore ks = null;
		InputStream keyStoreStream = null;
		try {
			ks = KeyStore.getInstance(type, "BC"); 
			keyStoreStream = new FileInputStream(location);
		    ks.load(keyStoreStream, password);
		} catch (Exception e) {
			if (keyStoreStream != null) {
				keyStoreStream.close();
			}
			ks = KeyStore.getInstance(type);
			keyStoreStream = new FileInputStream(location);
		    ks.load(keyStoreStream, password);
		    keyStoreStream.close();
		} finally {
			if (keyStoreStream != null) {
				keyStoreStream.close();
			}
		}
    	
    	return ks;
	}
	
	/**
	 * Loads a keystore
	 * 
	 * @param location
	 * @param type
	 * @param password
	 * @return Returns the keystore loaded from the given location.
	 */
	public static KeyStore openStore(String location, String type, 
			char[] password) throws GeneralSecurityException, IOException  {
		KeyStore ks = KeyStore.getInstance(type, "BC");
		InputStream keyStoreStream = 
			ClassLoader.getSystemClassLoader().getResourceAsStream(location);
	    ks.load(keyStoreStream, password);
	    keyStoreStream.close();
    	
    	return ks;
	}
	
	public static KeyPair generateKeyPair() throws GeneralSecurityException {
	    // generate a keypair for the new cert
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        return keyGen.generateKeyPair();
	}
	
	public static void usage() {
		System.out
				.println("\nUSAGE: " + CertTool.class.getName() + " <import | gen> <options>\n\n"
						
						+ "import options:\n" 
						+ "  Notes: Reads an X.509 certificate from one store and optionally places\n"
						+ "  it in another store as a trusted certificate.  If the output-store is not\n" 
						+ "  specified, then the certificate will be displayed to the console.\n\n"
						+ "  -base64-cert-file=<cert file path> | \n\n"
						+ "  -input-keystore=<keystore> \n"
						+ "  -input-keystore-pass=<keystore password> \n"
						+ "  -input-storetype=<storetype: PKCS12(default) | BKS> \n" 
						+ "  -input-alias=<alias> \n"  
						+ "[ -output-keystore=<keystore> \n"
						+ "  -output-keystore-pass=<keystore password> \n"
						+ "  -output-storetype=<storetype: PKCS12(default) | BKS> \n" 
						+ "  -output-alias=<trusted-alias> ] \n\n"

						+ "gen options:\n" 
						+ "  Notes: Generates a new X.509 certificate and corresponding keypair.\n"
						+ "  The input-keystore contains cert and key material for issuer.  If\n"
						+ "  not specified, the generated certificate is self-signed.  If output-store\n" 
						+ "  is not specified, then the certificate and keys will be displayed to the\n" 
						+ "  console.\n\n"
						
						+ "-dn=<distinguished name> \n"
						+ "[ -validity=<days (default:12 years)> ]\n"
						+ "[ -input-keystore=<keystore> \n"
						+ "  -input-keystore-pass=<keystore password> \n"
						+ "  -input-storetype=<storetype: PKCS12(default) | BKS> \n" 
						+ "  -input-alias=<alias> \n"  
						+ "  -input-entry-pass=<password> ] \n"
						+ "[ -output-keystore=<keystore> \n"
						+ "  -output-keystore-pass=<keystore password> \n"
						+ "  -output-storetype=<storetype: PKCS12(default) | BKS> \n" 
						+ "  -output-alias=<alias> \n"  
						+ "  -output-entry-pass=<password> ]");
	}

	public static void main(String args[]) throws Exception {
		
		boolean importCert = false;
		boolean genCert = false;
		
		String inputKeyStore = null;
		char[] inputKeyStorePass = null;
		String inputStoreType = "PKCS12";
		String inputAlias = null;
		char[] inputEntryPass = null;

		String outputKeyStore = null;
		char[] outputKeyStorePass = null;
		String outputStoreType = "PKCS12";
		String outputAlias = null;
		char[] outputEntryPass = null;
		
		String base64CertFile = null;

		String dn = null;
		long validity = ((365L * 12L) + 3L) * 24L * 60L * 60L * 1000L; 	// default: 12 years

		for (int i = 0; i < args.length; i++) {
			try {
				StringTokenizer tk = new StringTokenizer(args[i], "=");
				String option = tk.nextToken();
				String value = "";
				if (tk.hasMoreTokens()) {
					value = tk.nextToken();
				}

				if (option.equals("import")) {
					importCert = true;
				} else if (option.equals("gen")) {
					genCert = true;

				} else if (option.equals("-base64-cert-file")) {
					base64CertFile = value;
				} else if (option.equals("-input-keystore")) {
					inputKeyStore = value;
				} else if (option.equals("-input-keystore-pass")) {
					inputKeyStorePass = value.toCharArray();
				} else if (option.equals("-input-storetype")) {
					inputStoreType = value;
				} else if (option.equals("-input-alias")) {
					inputAlias = value;
				} else if (option.equals("-input-entry-pass")) {
					inputEntryPass = value.toCharArray();

				} else if (option.equals("-output-keystore")) {
					outputKeyStore = value;
				} else if (option.equals("-output-keystore-pass")) {
					outputKeyStorePass = value.toCharArray();
				} else if (option.equals("-output-storetype")) {
					outputStoreType = value;
				} else if (option.equals("-output-alias")) {
					outputAlias = value;
				} else if (option.equals("-output-entry-pass")) {
					outputEntryPass = value.toCharArray();

				} else if (option.equals("-dn")) {
					dn = value;
					dn += "=";
					dn += tk.nextToken();
					while (tk.hasMoreTokens()) {
						dn += "=";
						dn += tk.nextToken();
					}
				} else if (option.equals("-validity")) {
					validity = Integer.getInteger(value);
				} else {
					usage();
					return;
				}

			} catch (NoSuchElementException e) {
				e.printStackTrace();
				usage();
				return;
			}

		}

		if ((!genCert && !importCert) || (genCert && importCert)) {
			usage();
			return;
		}
		
		if (importCert) {
			
			// do import cert
			
			if (((base64CertFile == null) && (inputKeyStore == null)) ||  
				((base64CertFile != null) && (inputKeyStore != null))){
				usage();
				return;
			}
			java.security.cert.Certificate cert = null;
			
			if (base64CertFile != null) {
				// read in the cert from the binary cert file

				System.out.print("Retrieving import cert...");
				System.out.flush();

				java.io.FileInputStream fin = new java.io.FileInputStream(base64CertFile);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				cert = cf.generateCertificate(fin);

				System.out.println("Done.");

			} else {
			
				// open the keystore
				System.out.print("Retrieving import cert...");
				System.out.flush();
				KeyStore ks = KeyStore.getInstance(inputStoreType, "BC");
			    FileInputStream fis = new FileInputStream(inputKeyStore);
			    ks.load(fis, inputKeyStorePass);
		    	fis.close();
		    	
		    	cert = ks.getCertificate(inputAlias);
	
			    if (cert == null) {
			    	System.err.println("No such issuing certificate alias");
			    	return;
			    }
			    
				System.out.println("Done.");
			}
	
			// write the cert as a trusted cert, if necessary
			if (outputKeyStore != null) {
				System.out.print("Writing trusted certificate entry to output keystore...");
				System.out.flush();
	            KeyStore ks = KeyStore.getInstance(outputStoreType, "BC");
				File outFile = new File(outputKeyStore);
				if (outFile.exists()) {
				    FileInputStream fis = new FileInputStream(outputKeyStore);
		            ks.load(fis, outputKeyStorePass);
		            fis.close();
				} else {
		            ks.load(null, outputKeyStorePass);
				}
	            
	            java.security.KeyStore.TrustedCertificateEntry entry = new 
	            	java.security.KeyStore.TrustedCertificateEntry(cert);
	            
	            ks.setEntry(outputAlias, entry, null);
	            
	            FileOutputStream fos = new FileOutputStream(outputKeyStore);
	            ks.store(fos, outputKeyStorePass);
	            fos.close();
				System.out.println("Done.");
			} else {
		
				System.out.println("Cert:\n" + cert.toString());

			}
			
		} else {
		
			// do gen cert
				
		    // generate a keypair for the new cert
			System.out.print("Generating keypair...");
			System.out.flush();
	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
	        KeyPair keyPair = keyGen.generateKeyPair();
			System.out.println("Done.");
		    
	    	X509Certificate cert = null; 
	    	X509Certificate caCert = null;
	    	X509Certificate[] certChain = null;
		    if (inputKeyStore == null) {
		    	// create a self-signed cert
				System.out.print("Creating self-signed certificate...");
				System.out.flush();
		    	cert = createMasterCert(dn, validity, keyPair.getPublic(), keyPair.getPrivate());
		    	caCert = cert;
		    	certChain = new X509Certificate[]{cert};
				System.out.println("Done.");
		    } else {
				// open the keystore
				System.out.print("Retrieving issuer cert and private key...");
				System.out.flush();
				KeyStore ks = KeyStore.getInstance(inputStoreType, "BC");
			    FileInputStream fis = new FileInputStream(inputKeyStore);
			    ks.load(fis, inputKeyStorePass);
		    	fis.close();
	
		    	// load the signing cert/private key and generate a client cert
			    PrivateKey caPrivKey = (PrivateKey) ks.getKey(
			    		inputAlias, 
			    		inputEntryPass);
			    
			    java.security.cert.Certificate[] caCertChain = 
			    	(java.security.cert.Certificate[]) ks.getCertificateChain(inputAlias);
	
			    if ((caCertChain == null) || (caCertChain.length == 0)) {
			    	System.err.println("No such issuing certificate alias");
			    	return;
			    }
			    caCert = (X509Certificate) caCertChain[0]; 
			    
				System.out.println("Done.");
			    
				System.out.print("Creating issuer-signed certificate...");
				System.out.flush();
				cert = createIntermediateCert(dn, validity, keyPair.getPublic(), caPrivKey, caCert);
				System.out.println("Done.");
			    
			    certChain = new X509Certificate[caCertChain.length + 1];
			    certChain[0] = cert;
			    for (int i = 0; i < caCertChain.length; i++) {
			    	certChain[i + 1] = (X509Certificate) caCertChain[i];
			    }
		    }
	
			System.out.print("Checking validity of new certificate...");
			System.out.flush();
		    cert.checkValidity(new Date());
			cert.verify(caCert.getPublicKey());
			System.out.println("Done.");
	
			if (outputKeyStore != null) {
				System.out.print("Writing new certificate and keypair to output keystore...");
				System.out.flush();
	            KeyStore ks = KeyStore.getInstance(outputStoreType, "BC");
	            ks.load(null, outputKeyStorePass);
	            ks.setKeyEntry(outputAlias, keyPair.getPrivate(), outputEntryPass, certChain);
	            
	            FileOutputStream fos = new FileOutputStream(outputKeyStore);
	            ks.store(fos, outputKeyStorePass);
	            fos.close();
				System.out.println("Done.");
			} else {
				for (X509Certificate a : certChain) {
					System.out.println("Cert:\n" + a.toString());
				}
			}
		}
	}
}
