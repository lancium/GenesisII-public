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
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.common.notification.Notify;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.resolver.simple.CreateResolverRequestType;
import edu.virginia.vcgr.genii.resolver.simple.CreateResolverResponseType;
import edu.virginia.vcgr.genii.resolver.simple.InvalidWSNameFaultType;
import edu.virginia.vcgr.genii.resolver.simple.SimpleResolverFactoryPortType;
import edu.virginia.vcgr.genii.resolver.simple.SimpleResolverPortType;

public class SimpleResolverFactoryServiceImpl extends GenesisIIBase implements SimpleResolverFactoryPortType
{	
	static private Log _logger = LogFactory.getLog(SimpleResolverFactoryServiceImpl.class);
	static private String _simpleResolverServiceURL = null;
	
	public SimpleResolverFactoryServiceImpl() throws RemoteException
	{
		this("SimpleResolverFactoryPortType");
	}
	
	protected SimpleResolverFactoryServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes.GENII_SIMPLE_RESOLVER_FACTORY_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.GENII_NOTIFICATION_CONSUMER_PORT_TYPE);
	}
	
	public QName getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.GENII_SIMPLE_RESOLVER_FACTORY_PORT_TYPE;
	}
	
	private synchronized String getResolverServiceURL()
	{
		if (_simpleResolverServiceURL == null)
			_simpleResolverServiceURL = Container.getServiceURL("SimpleResolverPortType");
		return _simpleResolverServiceURL;
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateResolverResponseType createResolver(CreateResolverRequestType createResolver)
		throws RemoteException,
			ResourceException,
			InvalidWSNameFaultType
	{
		_logger.debug("createResolver called");

		EndpointReferenceType resolverReference = null;
		EndpointReferenceType resolutionEPR = null;
		
		EndpointReferenceType entryReference = createResolver.getTarget_EPR();
		WSName inputName = new WSName(entryReference);
		
		if (!inputName.isValidWSName())
			throw new InvalidWSNameFaultType();

		String factoryEPI = null;
		EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.EPR_PROPERTY_NAME);
		if (myEPR != null)
		{
			WSName myName = new WSName(myEPR);
			if (myName.isValidWSName())
			{
				factoryEPI = myName.getEndpointIdentifier().toString();
			}
		}

		/* create new SimpleResolver resource */
		HashMap<QName, MessageElement> constructionParameters =
			new HashMap<QName, MessageElement>();
		SimpleResolverConstructionParameters.insertSimpleResolverParameters(
			constructionParameters, 
			entryReference,
			factoryEPI);
		MessageElement []params = new MessageElement[constructionParameters.size()];
		constructionParameters.values().toArray(params);

		try
		{
			SimpleResolverPortType resolverService = ClientUtils.createProxy(
					SimpleResolverPortType.class,
					EPRUtils.makeEPR(getResolverServiceURL()));
			VcgrCreateResponse resp = resolverService.vcgrCreate(new VcgrCreate(params));
			resolverReference = resp.getEndpoint();
			SimpleResolverPortType resolver = ClientUtils.createProxy(
					SimpleResolverPortType.class, resolverReference);
			resolutionEPR = resolver.resolve(null);
		}
		catch(Throwable t)
		{
			throw new ResourceException("Could not create new SimpleResolver", t);
		}
		
		return new CreateResolverResponseType(resolutionEPR, resolverReference);
	}
	

	/* NotificationConsumer port type */
	@RWXMapping(RWXCategory.OPEN)
	public void notify(Notify notify) throws RemoteException, ResourceUnknownFaultType
	{
		/* nothing for now */
	}
}
