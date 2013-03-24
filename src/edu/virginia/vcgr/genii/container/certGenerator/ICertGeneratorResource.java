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
package edu.virginia.vcgr.genii.container.certGenerator;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface ICertGeneratorResource extends IResource
{
	public void setCertificateIssuerInfo(HashMap<QName, Object> creationParameters) throws ResourceException;

	public Long getDefaultValidity() throws ResourceException;

	public X509Certificate[] getIssuerChain() throws ResourceException;

	public PrivateKey getIssuerPrivateKey() throws ResourceException;
}