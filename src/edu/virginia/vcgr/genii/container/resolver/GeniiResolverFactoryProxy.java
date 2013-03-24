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

import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.resolver.GeniiResolverPortType;
import edu.virginia.vcgr.genii.resolver.InvalidWSNameFaultType;

public class GeniiResolverFactoryProxy implements IResolverFactoryProxy
{
	/**
	 * Register the given EPR with a resolver. Return an EPR with targetEPR's address and
	 * resource-key, and with a resolver element.
	 * 
	 * @param confProperties
	 *            The properties from server-config.xml.
	 * @param resolverProperties
	 *            The properties from postCreate().
	 */
	public EndpointReferenceType createResolver(EndpointReferenceType targetEPR, Properties confProperties,
		MessageElement[] resolverProperties) throws RemoteException, ResourceException, InvalidWSNameFaultType
	{
		MessageElement[] params = new MessageElement[1];
		params[0] = new MessageElement(GeniiResolverServiceImpl.TARGET_EPR_PARAMETER, targetEPR);
		String resolverServiceURL = Container.getServiceURL("GeniiResolverPortType");
		EndpointReferenceType resolverServiceEPR = EPRUtils.makeEPR(resolverServiceURL);
		GeniiResolverPortType resolverService = ClientUtils.createProxy(GeniiResolverPortType.class, resolverServiceEPR);
		VcgrCreateResponse response = resolverService.vcgrCreate(new VcgrCreate(params));
		EndpointReferenceType resolutionEPR = response.getEndpoint();
		GeniiResolverPortType resolutionService = ClientUtils.createProxy(GeniiResolverPortType.class, resolutionEPR);
		return resolutionService.resolve(null);
	}
}
