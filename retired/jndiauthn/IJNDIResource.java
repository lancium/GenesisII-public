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
package edu.virginia.vcgr.genii.container.jndiauthn;

import org.apache.axis.types.URI;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;

public interface IJNDIResource extends IRNSResource
{

	static public QName IS_IDP_RESOURCE_CONSTRUCTION_PARAM = new QName(GenesisIIConstants.GENESISII_NS, "is-idp-resource");

	static public enum StsType {
		NIS, LDAP
	};

	public boolean isIdpResource();

	public String getIdpName() throws ResourceException;

	public StsType getStsType() throws ResourceException;

	public URI createChildIdpEpi(String childName) throws URI.MalformedURIException, ResourceException;
}