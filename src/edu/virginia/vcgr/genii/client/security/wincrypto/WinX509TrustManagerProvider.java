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

import java.security.Security;


/**
 * Provider class for the X.509 MS Windows Trust Manager
 * 
 * @author Duane Merrill
 */
public class WinX509TrustManagerProvider extends java.security.Provider {
	static final long serialVersionUID = 0L;
	/** String identifiying the instance-name to be supplied to TrustManagerFactory
	  * for obtaining a WinX509TrustManagerFactory */
	public static final String TM_NAME = "WinX509TM";
	

	/** Constructor */
	public WinX509TrustManagerProvider() { 
		super(TM_NAME, 1.0, TM_NAME + " implements MS Windows Trust Factory"); 
		
		put("TrustManagerFactory." + TM_NAME, WinX509TrustManagerFactorySpi.class.getName()); 
	}


	/**
	 * Installs the WinX509TrustManagerProvider into the provider hierarchy
	 * if it was not done in the JDK/JRE's lib/security/java.security file.
	 * Using this mechanism may require that the codebase comprising this code
	 * be signed appropriately
	 */
	public static synchronized void install() { 
		
		if (Security.getProvider(TM_NAME) == null) {
			Security.insertProviderAt (new WinX509TrustManagerProvider(), 1);
		}
	} 
}

