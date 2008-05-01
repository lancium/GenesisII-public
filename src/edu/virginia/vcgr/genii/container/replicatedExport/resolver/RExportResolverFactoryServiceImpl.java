/*
 * Copyright 2007 University of Virginia
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

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.common.notification.Notify;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverFactoryPortType;
import edu.virginia.vcgr.genii.resolver.simple.CreateResolverRequestType;
import edu.virginia.vcgr.genii.resolver.simple.CreateResolverResponseType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.InvalidWSNameFaultType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverPortType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.UpdateRequestType;
import edu.virginia.vcgr.genii.replicatedExport.resolver.CreateRootReplicaRequest;

public class RExportResolverFactoryServiceImpl extends GenesisIIBase implements RExportResolverFactoryPortType
{
	static private Log _logger = LogFactory.getLog(RExportResolverFactoryServiceImpl.class);
	
	public RExportResolverFactoryServiceImpl() 
		throws RemoteException
	{
		this("RExportResolverFactoryPortType");
	}
	
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.REXPORT_RESOLVER_FACTORY_PORT_TYPE;
	}
	
	protected RExportResolverFactoryServiceImpl(String serviceName) 
		throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(
				WellKnownPortTypes.REXPORT_RESOLVER_FACTORY_PORT_TYPE);
		addImplementedPortType(
				WellKnownPortTypes.GENII_NOTIFICATION_CONSUMER_PORT_TYPE);
	}
	
	/* NotificationConsumer port type */
	@RWXMapping(RWXCategory.OPEN)
	public void notify(Notify notify) throws RemoteException, ResourceUnknownFaultType
	{
		/* nothing for now */
	}
	
	/**
	 * Creates resolver resource for export resource specified by targetEPR in request
	 * 
	 * @param targetEPR:	export resource for which a resolver is to be created
	 * @param any():		resolverCreationProperties
	 *  					primary localpath
	 *  					resolver factory epr
	 *  
	 *  
	 * @return resolutionEPR: 	target EPR augmented with resolver EPR
	 * @return resolverEPR:	resolver EPR
	 * 
	 */
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateResolverResponseType createResolver(CreateResolverRequestType createResolver)
		throws RemoteException,
			ResourceException,
			InvalidWSNameFaultType
	{
		//if no creation params for resolver, do not create resolver
		if (createResolver.get_any() == null){
			return null;
		}	
		
		_logger.debug("createRExportResolver called");
	
		EndpointReferenceType resolverReference = null;
		EndpointReferenceType resolutionEPR = null;
		
		//get primaryEPR - epr to which resolver is to be added
		EndpointReferenceType primaryEPR = createResolver.getTarget_EPR();
		
		//collect creation params for resolver
		MessageElement []resolverCreationProperties = RExportResolverUtils.
			createResolverCreationParams(createResolver.get_any(), primaryEPR);
		
		try
		{ 
			//create proxy to resolver specifed by creation param
			RExportResolverPortType resolverService = ClientUtils.createProxy(
					RExportResolverPortType.class,
					RExportResolverUtils.extractResolverServiceEPR(createResolver.get_any()));
			//previously: EPRUtils.makeEPR(getResolverServiceURL()
			
			//create resolver instance with params
			VcgrCreateResponse resp = resolverService.vcgrCreate(
				new VcgrCreate(resolverCreationProperties));
			
			//get resolver epr
			resolverReference = resp.getEndpoint();
			
			//store resource-resolver mapping in table
			RExportResolverUtils.updateResolverResourceInfo(
					resolverReference,
					primaryEPR);
			
			//create proxy to resolver
			RExportResolverPortType resolverProxy = ClientUtils.createProxy(
					RExportResolverPortType.class, 
					resolverReference);
			
			//get resolution epr returned from resolver
			resolutionEPR = resolverProxy.update(new UpdateRequestType(primaryEPR)).getResolution_EPR();
			
			
			//create rexportdir replica if default resolver
			if (RExportResolverUtils.isRootResolver(createResolver.get_any())){
				resolverProxy.createRootReplica(new CreateRootReplicaRequest(primaryEPR));
			}
		}
		catch(Throwable t){
			throw new ResourceException(
					"Could not create new RExportResolver", t);
		}
		
		return new CreateResolverResponseType(resolutionEPR, resolverReference);
	}
}

















