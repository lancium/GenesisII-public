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

import java.net.URI;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class SimpleResolverEntry
{
	private URI _targetEPI;
	private EndpointReferenceType _targetEPR;
	private int _version;
	private String _subscriptionGUID;
	private EndpointReferenceType _terminateSubscription;
	private URI _factoryEPI;
	private URI _resolverEPI;
	private EndpointReferenceType _resolverEPR;
	private MessageElement []_attributes;
	
	public SimpleResolverEntry(URI targetEPI, EndpointReferenceType targetEPR, int version, String subscriptionGUID,
			EndpointReferenceType terminateSubscription, URI factoryEPI, URI resolverEPI, EndpointReferenceType resolverEPR, MessageElement []attributes)
	{
		_targetEPI = targetEPI;
		_targetEPR = targetEPR;
		_version = version;
		_subscriptionGUID = subscriptionGUID;
		_attributes = attributes;
		_factoryEPI = factoryEPI;
		_resolverEPI = resolverEPI;
		_resolverEPR = resolverEPR;
		_terminateSubscription = terminateSubscription;
	}
	
	public SimpleResolverEntry(EndpointReferenceType targetEPR, int version,  String subscriptionGUID,
			EndpointReferenceType terminateSubscription, URI factoryEPI, URI resolverEPI, EndpointReferenceType resolverEPR, MessageElement []attributes)
		throws ResourceException
	{
		WSName tmp = new WSName(targetEPR);
		if (tmp.isValidWSName())
			_targetEPI = tmp.getEndpointIdentifier();
		else
			throw new ResourceException("Invalid EPR for SimpleResolverEntry - not a WSName");
		_targetEPR = targetEPR;
		_version = version;
		_subscriptionGUID = subscriptionGUID;
		_terminateSubscription = terminateSubscription;
		_factoryEPI = factoryEPI;
		_resolverEPI = resolverEPI;
		_resolverEPR = resolverEPR;
		_attributes = attributes;
	}
		
	public SimpleResolverEntry(URI targetEPI, EndpointReferenceType targetEPR, int version, String subscriptionGUID, 
			EndpointReferenceType terminateSubscription, URI factoryEPI, URI resolverEPI, EndpointReferenceType resolverEPR)
	{
		this(targetEPI, targetEPR, version, subscriptionGUID, terminateSubscription, factoryEPI, resolverEPI, resolverEPR, null);
	}
	
	public URI getTargetEPI()
	{
		return _targetEPI;
	}
	
	public void setTargetEPI(URI targetEPI)
	{
		_targetEPI = targetEPI;
	}
	
	public EndpointReferenceType getTargetEPR()
	{
		return _targetEPR;
	}
	
	public void setTargetEPR(EndpointReferenceType targetEPR)
	{
		_targetEPR = targetEPR;
	}
	
	public int getVersion()
	{
		return _version;
	}
	
	public void incrementVersion()
	{
		_version++;
	}
	
	public EndpointReferenceType getTerminateSubscription()
	{
		return _terminateSubscription;
	}
	
	public void setTerminateSubscription(EndpointReferenceType terminateSubscription)
	{
		_terminateSubscription = terminateSubscription;
	}
	
	public String getSubscriptionGUID()
	{
		return _subscriptionGUID;
	}
	
	public void setSubscriptionGUID(String subscriptionGUID)
	{
		_subscriptionGUID = subscriptionGUID;
	}
	
	public URI getFactoryEPI()
	{
		return _factoryEPI;
	}
	
	public void setFactoryEPI(URI factoryEPI)
	{
		_factoryEPI = factoryEPI;
	}
	
	public URI getResolverEPI()
	{
		return _resolverEPI;
	}
	
	public void setResolverEPI(URI resolverEPI)
	{
		_resolverEPI = resolverEPI;
	}
	
	public EndpointReferenceType getResolverEPR()
	{
		return _resolverEPR;
	}
	
	public void setResolverEPR(EndpointReferenceType resolverEPR)
	{
		_resolverEPR = resolverEPR;
	}
	
	public MessageElement[] getAttributes()
	{
		return _attributes;
	}
	
	public void setAttributes(MessageElement[] attributes)
	{
		_attributes = attributes;
	}
}
