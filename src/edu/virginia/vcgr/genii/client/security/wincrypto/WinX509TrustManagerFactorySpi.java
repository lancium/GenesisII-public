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

import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.security.KeyStoreException;
import java.security.KeyStore;

/**
 * This class defines the Service Provider Interface (SPI) for the WinX509TM TrustManagerFactory
 * class.
 * 
 * @author Duane Merrill
 * 
 */
public final class WinX509TrustManagerFactorySpi extends TrustManagerFactorySpi
{

	/** Wrapped X509TrustManager */
	private X509TrustManager _trustManager;

	/**
	 * Returns one trust manager for each type of trust material.
	 */
	protected TrustManager[] engineGetTrustManagers()
	{
		return new TrustManager[] { _trustManager };
	}

	/**
	 * Initializes this factory with a source of certificate authorities and related trust material.
	 */
	synchronized protected void engineInit(KeyStore ks) throws KeyStoreException
	{
		if (_trustManager == null) {
			_trustManager = new WinX509TrustManager();
		}
	}

	/**
	 * Initializes this factory with a source of provider-specific key material.
	 */
	synchronized protected void engineInit(ManagerFactoryParameters spec)
	{
		if (_trustManager == null) {
			_trustManager = new WinX509TrustManager();
		}
	}
}
