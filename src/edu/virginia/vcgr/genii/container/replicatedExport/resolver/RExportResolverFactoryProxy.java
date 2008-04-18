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

package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.axis.message.MessageElement;

import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.resolver.simple.CreateResolverRequestType;
import edu.virginia.vcgr.genii.resolver.simple.CreateResolverResponseType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.InvalidWSNameFaultType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverFactoryPortType;
import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resolver.Resolution;

public class RExportResolverFactoryProxy implements IResolverFactoryProxy
{	
	public Resolution createResolver(EndpointReferenceType targetEPR, Properties params,
			MessageElement []creationProps)
		throws RemoteException,	ResourceException, InvalidWSNameFaultType
	{
		EndpointReferenceType resolverReference = null;
		EndpointReferenceType resolutionEPR = null;
		
		try{
			RExportResolverFactoryPortType resolverFactoryService = 
				ClientUtils.createProxy(
					RExportResolverFactoryPortType.class,
					EPRUtils.makeEPR(Container.getServiceURL(
							"RExportResolverFactoryPortType")));
			CreateResolverResponseType resp = resolverFactoryService.createResolver(
					new CreateResolverRequestType(targetEPR, creationProps));
			
			if (resp == null)
				return null;
			
			resolverReference = resp.getResolver_EPR();
			resolutionEPR = resp.getResolution_EPR();
		}
		catch(ConfigurationException ce){
			throw new ResourceException(
					"Factory could not create RExortResolver.", ce);
		}
		
		return new Resolution(resolutionEPR, resolverReference);
	}
}
