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

package edu.virginia.vcgr.genii.client.security.authz;

import java.io.*;

import edu.virginia.vcgr.genii.common.security.*;

/**
 * Interface of client-tooling for authorization modules 
 * 
 * @author dgm4d
 */
public interface AuthZSubTool
{

	// Methods to handle resource AuthZ configurations

	public void displayAuthZConfig(
			AuthZConfig config, 
			PrintWriter out,
			PrintWriter err, 
			BufferedReader in) throws AuthZSecurityException;

	public AuthZConfig modifyAuthZConfig(
			AuthZConfig config, 
			PrintWriter out,
			PrintWriter err, 
			BufferedReader in) 
		throws IOException,	AuthZSecurityException;

	public AuthZConfig getEmptyAuthZConfig() throws AuthZSecurityException;

}
