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

import org.apache.axis.types.URI;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.MetadataMappingType;
import org.ggf.rns.NameMappingType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.rns.WriteNotPermittedFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.GenesisIIBaseTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ResourceTerminationContents;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;

import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.RNSContainerUtilities;
import edu.virginia.vcgr.genii.resolver.simple.InvalidWSNameFaultType;
import edu.virginia.vcgr.genii.resolver.simple.SimpleResolverPortType;
import edu.virginia.vcgr.genii.resolver.simple.UpdateRequestType;
import edu.virginia.vcgr.genii.resolver.simple.UpdateResponseType;
import edu.virginia.vcgr.genii.security.RWXCategory;

@GeniiServiceConfiguration(
	resourceProvider=SimpleResolverDBResourceProvider.class)
public class SimpleResolverServiceImpl extends GenesisIIBase
	implements SimpleResolverPortType
{	
	static private Log _logger = LogFactory.getLog(SimpleResolverServiceImpl.class);
	static public QName SIMPLE_RESOLVER_TARGET_CONSTRUCTION_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "simple-resolver-target-epr");
	static public QName SIMPLE_RESOLVER_FACTORY_EPI_CONSTRUCTION_PARAMETER =
		new QName(GenesisIIConstants.GENESISII_NS, "simple-resolver-factory-epi");
	
	public SimpleResolverServiceImpl() throws RemoteException
	{
		this("SimpleResolverPortType");
	}
	
	protected SimpleResolverServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes.ENDPOINT_IDENTIFIER_RESOLVER_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.REFERENCE_RESOLVER_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.GENII_SIMPLE_RESOLVER_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.RNS_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.GENII_NOTIFICATION_CONSUMER_PORT_TYPE);
	}
	
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.GENII_SIMPLE_RESOLVER_PORT_TYPE;
	}
	
		
	/* EndpointIdentifierResolver port type. */
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType resolveEPI(org.apache.axis.types.URI resolveEPI) 
		throws RemoteException,
			ResourceUnknownFaultType, 
			ResolveFailedFaultType
	{
		_logger.debug("Entered resolveEPI method.");
		
		ISimpleResolverResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (ISimpleResolverResource)rKey.dereference();
		
		SimpleResolverEntry entry = resource.getEntry();
		EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.EPR_PROPERTY_NAME);

		URI epi = null;
		try
		{
			epi = new URI(resolveEPI.toString());
		}
		catch(Throwable t)
		{
			_logger.warn("Bad URI type passed into resolveEPI", t);
			throw new ResolveFailedFaultType();
		}
		
		if (epi == null || !epi.equals(entry.getTargetEPI()))
			throw new ResolveFailedFaultType();
		
		return SimpleResolverUtils.createResolutionEPR(entry.getTargetEPR(), myEPR);
	}
		
	/* ReferenceResolver port type. */
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType resolve(Object resolveRequest) 
		throws RemoteException,
			ResourceUnknownFaultType, 
			ResolveFailedFaultType
	{
		_logger.debug("Entered resolve method.");
		
		ISimpleResolverResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (ISimpleResolverResource)rKey.dereference();
		
		SimpleResolverEntry entry = resource.getEntry();
		EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.EPR_PROPERTY_NAME);
		
		return SimpleResolverUtils.createResolutionEPR(entry.getTargetEPR(), myEPR);
	}
	
	
	/* SimpleResolver (mgmt) port type */
	@RWXMapping(RWXCategory.WRITE)
	public UpdateResponseType update(UpdateRequestType updateRequest)
		throws RemoteException,
			ResourceUnknownFaultType,
			InvalidWSNameFaultType
	{
		ISimpleResolverResource resource = null;
		EndpointReferenceType entryReference = null;
		URI targetEPI = null;

		entryReference = updateRequest.getNew_EPR();
		WSName inputName = new WSName(entryReference);
		
		if (!inputName.isValidWSName())
			throw new InvalidWSNameFaultType();
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (ISimpleResolverResource)rKey.dereference();
		
		SimpleResolverEntry entry = resource.getEntry();
		
		targetEPI = inputName.getEndpointIdentifier();
		entry.setTargetEPI(targetEPI);
		entry.setTargetEPR(entryReference);
		entry.incrementVersion();

		/* save changes */
		EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.EPR_PROPERTY_NAME);
		resource.update(entry);
		resource.commit();

		return new UpdateResponseType(SimpleResolverUtils.createResolutionEPR(entry.getTargetEPR(), myEPR));
	}

	/* RNS port type */
	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] add(RNSEntryType []request) throws RemoteException
	{
		throw new RemoteException("\"add\" not applicable.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public LookupResponseType lookup(String []lookupRequest)
		throws RemoteException, ResourceException
    {
		if (lookupRequest == null || lookupRequest.length == 0)
			lookupRequest = new String[] { null };
		
		Collection<RNSEntryResponseType> result =
			new LinkedList<RNSEntryResponseType>();
		
		ISimpleResolverResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (ISimpleResolverResource)rKey.dereference();
		
		// if service, list all simple resolvers (Resolver EPI --> Resolver EPR)
		if (resource.getKey() == null)
		{
			HashMap<String, EndpointReferenceType> resolvers = resource.listAllResolvers();
			
			if (resolvers != null && resolvers.size() > 0)
			{
				Iterator<String> epiIter = resolvers.keySet().iterator();
				
				while (epiIter.hasNext())
				{
					String nextEPI = epiIter.next();
					for (String request : lookupRequest)
					{
						if (request == null || request.equals(nextEPI))
						{
							EndpointReferenceType nextEPR = resolvers.get(nextEPI);
							result.add(new RNSEntryResponseType(
								nextEPR, null, null, nextEPI));
							break;
						}
					}
				}
			}
		} else
		{
			// if resolver instance, return entry with name=EPI and value=OriginalEPR 
			SimpleResolverEntry entry = resource.getEntry();
		
			for (String request : lookupRequest)
			{
				if (request == null || request.equals(entry.getTargetEPI().toString()))
					result.add(new RNSEntryResponseType(
						entry.getTargetEPR(), null, null, entry.getTargetEPI().toString()));
			}
		}

		return RNSContainerUtilities.translate(
    		result, iteratorBuilder(
    			RNSEntryResponseType.getTypeDesc().getXmlType()));
    }

	
	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] rename(NameMappingType[] renameRequest)
		throws RemoteException, WriteNotPermittedFaultType
	{
		throw new RemoteException("\"rename\" not applicable.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] remove(String[] removeRequest)
		throws RemoteException, WriteNotPermittedFaultType
	{
		throw new RemoteException("\"remove\" not applicable.");
	}

	
	@Override
	protected void registerNotificationHandlers(
			NotificationMultiplexer multiplexer)
	{
		super.registerNotificationHandlers(multiplexer);
		
		multiplexer.registerNotificationHandler(
			GenesisIIBaseTopics.RESOURCE_TERMINATION_TOPIC.asConcreteQueryExpression(),
			new LegacyResourceTerminationNotificationHandler());
	}


	private class LegacyResourceTerminationNotificationHandler
		extends AbstractNotificationHandler<ResourceTerminationContents>
	{
		private LegacyResourceTerminationNotificationHandler()
		{
			super(ResourceTerminationContents.class);
		}

		@Override
		public String handleNotification(TopicPath topic,
			EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference,
			ResourceTerminationContents contents) throws Exception
		{
			SimpleResolverTerminateUserData notifyData = 
				contents.additionalUserData(
					SimpleResolverTerminateUserData.class);
			
			/* check if EPI, version, and ID match */
			ResourceKey rKey = ResourceManager.getCurrentResource();
			ISimpleResolverResource resource = (ISimpleResolverResource) rKey.dereference();
			SimpleResolverEntry entry = resource.getEntry();

			if (entry.getTargetEPI().equals(notifyData.getEPI())  &&
					entry.getVersion() == notifyData.getVersion() &&
					entry.getSubscriptionGUID().equals(notifyData.getSubscriptionGUID()))
			{
				/* kill myself */
				destroy(new Destroy());
			}
			return NotificationConstants.OK;
		}
	}

	/* create/initialization helper methods */
	public void postCreate(ResourceKey rKey, EndpointReferenceType myEPR,
		ConstructionParameters cParams, HashMap<QName, Object> constructionParameters,
		Collection<MessageElement> resolverCreationParams)
			throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, myEPR, cParams, constructionParameters, resolverCreationParams);
		
		/* grab targetEPR construction parameter and initialize resource with its information */
		EndpointReferenceType targetEPR = (EndpointReferenceType) constructionParameters.get(SIMPLE_RESOLVER_TARGET_CONSTRUCTION_PARAMETER);
		if (targetEPR == null)
			throw new ResourceException("Invalid construction parameters for SimpleResolverDBResource.inisitialize()");

		/* grab targetEPR construction parameter and initialize resource with its information */
		URI factoryEPI = (URI) constructionParameters.get(SIMPLE_RESOLVER_FACTORY_EPI_CONSTRUCTION_PARAMETER);
		if (factoryEPI == null)
			throw new ResourceException("Invalid construction parameters for SimpleResolverDBResource.inisitialize()");

		URI myEPI = null;
		WSName myName = new WSName(myEPR);
		if (myName.isValidWSName())
			myEPI = myName.getEndpointIdentifier();
		
		SimpleResolverEntry entry = new SimpleResolverEntry(targetEPR, 1, null, null, factoryEPI, myEPI, myEPR, null);
		
		((SimpleResolverDBResource) rKey.dereference()).update(entry);
		
		((SimpleResolverDBResource) rKey.dereference()).commit();
	}

	protected Object translateConstructionParameter(MessageElement parameter)
		throws Exception
	{
		QName messageName = parameter.getQName();
		if (messageName.equals(SIMPLE_RESOLVER_TARGET_CONSTRUCTION_PARAMETER))
			return parameter.getObjectValue(EndpointReferenceType.class);
		else if (messageName.equals(SIMPLE_RESOLVER_FACTORY_EPI_CONSTRUCTION_PARAMETER))
		{
			String factoryEPI = (String) parameter.getObjectValue(String.class);
			return new URI(factoryEPI);
		}
		else
			return super.translateConstructionParameter(parameter);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] setMetadata(
		MetadataMappingType[] setMetadataRequest) throws RemoteException,
			WriteNotPermittedFaultType
	{
		throw new RemoteException("setMetadata operation not supported!");
	}
}
