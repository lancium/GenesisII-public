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


import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.KeyManager;
import java.security.KeyStoreException;
import java.security.KeyStore; 


/**
 * This class defines the Service Provider Interface (SPI) for the 
 * WinX509KM KeyManagerFactory class.
 * 
 * @author Duane Merrill
 *
 */
public final class WinX509KeyManagerFactorySpi extends KeyManagerFactorySpi { 
	
	/** Wrapped X509KeyManager */
	private X509KeyManager _keyManager;

	/**
	 * Initializes this factory with a source of key material
	 */
	protected synchronized void engineInit(KeyStore ks, char[] passphrase) throws KeyStoreException { 
		if (_keyManager == null) {
			_keyManager = new WinX509KeyManager();
		}
	} 

	/**
	 * Initializes this factory with a source of key material.
	 */
	protected synchronized void engineInit(ManagerFactoryParameters spec)  {
		if (_keyManager == null) {
			_keyManager = new WinX509KeyManager();
		}
	}

	/**
	 * Returns one key manager for each type of key material.
	 */
	protected KeyManager[] engineGetKeyManagers() { 
		return new KeyManager[] {_keyManager}; 
	} 
}
