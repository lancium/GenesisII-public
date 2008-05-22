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

import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

public class AuthZSecurityException extends GenesisIISecurityException
{
	static final long serialVersionUID = 0;

	protected Throwable _myCause = null;

	public AuthZSecurityException(String msg)
	{
		super(msg);
	}

	public AuthZSecurityException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

}