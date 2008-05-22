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

import java.security.cert.X509Certificate;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.security.MessageLevelSecurity;

public class MessageSecurityData
{
	public MessageLevelSecurity _neededMsgSec;
	public X509Certificate[] _resourceCertChain;
	public URI _resourceEpi;

	public MessageSecurityData(MessageLevelSecurity neededMsgSec,
			X509Certificate[] resourceCertChain, URI resourceEpi)
	{

		_resourceCertChain = resourceCertChain;
		_neededMsgSec = neededMsgSec;
		_resourceEpi = resourceEpi;

	}
}