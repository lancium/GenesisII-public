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

package edu.virginia.vcgr.genii.client.security.gamlauthz;

import java.io.*;
import java.lang.reflect.Method;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.comm.axis.security.MessageSecurityData;

public interface AuthZSubTool
{

	// Methods to handle resource AuthZ configurations

	public void displayAuthZConfig(AuthZConfig config, PrintStream out,
			PrintStream err, BufferedReader in) throws AuthZSecurityException;

	public AuthZConfig modifyAuthZConfig(AuthZConfig config, PrintStream out,
			PrintStream err, BufferedReader in) throws AuthZSecurityException,
			IOException;

	public AuthZConfig getEmptyAuthZConfig() throws AuthZSecurityException;

	// Methods to handle outgoing credentials

	/**
	 * Intended to manipulate the calling context as necessary before it is sent
	 * in an outgoing message
	 */
	public void messageSendPrepareHandler(ICallingContext callingContext,
			GamlCredential credential, MessageSecurityData msgSecData,
			Method method) throws GenesisIISecurityException;

	/**
	 * Displays the credential to the specified output streams
	 */
	public void displayWhoami(GamlCredential credential, PrintStream out,
			PrintStream err) throws GenesisIISecurityException;

}
