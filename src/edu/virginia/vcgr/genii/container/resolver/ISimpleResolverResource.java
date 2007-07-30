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
package edu.virginia.vcgr.genii.container.resolver;

import java.util.HashMap;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface ISimpleResolverResource extends IResource
{
	public void update(SimpleResolverEntry entry) 
		throws ResourceException, ResourceUnknownFaultType;
	public SimpleResolverEntry getEntry() throws ResourceException;
	public HashMap<String, EndpointReferenceType> listAllResolvers() 
		throws ResourceException;

}