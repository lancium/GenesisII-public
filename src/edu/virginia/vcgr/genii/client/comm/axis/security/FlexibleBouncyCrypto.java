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
package edu.virginia.vcgr.genii.client.comm.axis.security;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;

public class FlexibleBouncyCrypto extends Merlin
{
	static {
		CertTool.loadBCProvider();
	}
	
    /**
     * Lookup a X509 Certificate in the keystore according to a given serial number and
     * the issuer of a Certficate.
     * <p/>
     * The search gets all alias names of the keystore and gets the certificate chain
     * for each alias. Then the SerialNumber and Issuer fo each certificate of the chain
     * is compared with the parameters.
     *
     * @param issuer       The issuer's name for the certificate
     * @param serialNumber The serial number of the certificate from the named issuer
     * @return alias name of the certificate that matches serialNumber and issuer name
     *         or null if no such certificate was found.
     */
    public String getAliasForX509Cert(String issuer, BigInteger serialNumber)
            throws WSSecurityException {
        return getAliasForX509Cert(issuer);
    }
	
	public FlexibleBouncyCrypto() throws CredentialException,
			IOException {
		
		super(null);
		
    	properties = new Properties();
    	properties.setProperty("org.apache.ws.security.crypto.merlin.cert.provider", "BC");
	}
	
	public FlexibleBouncyCrypto(String name) throws CredentialException, IOException 
	{
		this();
	}
	
}