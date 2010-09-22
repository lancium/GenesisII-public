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
package edu.virginia.vcgr.genii.client.comm.axis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.GenesisIIEndpointInformation;
import edu.virginia.vcgr.genii.client.comm.IProxyFactory;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

public class AxisBasedProxyFactory implements IProxyFactory
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(AxisBasedProxyFactory.class);
	
	static public QName LOCATOR_REGISTRY_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "locator-registry");
	
	@SuppressWarnings("unchecked")
	public <IFace> IFace createProxy(ClassLoader loader, Class<IFace> iface, 
		EndpointReferenceType epr, ICallingContext callContext) 
		throws ResourceException, GenesisIISecurityException
	{
		XMLConfiguration conf = 
			ConfigurationManager.getCurrentConfiguration().getClientConfiguration();
		HashMap<String, Class<?>> _locators;
		
		synchronized(conf)
		{
			_locators = (HashMap<String, Class<?>>)conf.retrieveSection(
				LOCATOR_REGISTRY_QNAME);
		}
		
		Class<?> []locators = new Class[1];
		
		locators[0] = _locators.get(iface.getName());
		if (locators[0] == null)
		{
			throw new ConfigurationException(
				"Unable to find a locator for \"" + iface.getName() +
				"\".");
		}
		
		return (IFace)createClientProxy(loader, locators, epr, callContext);
	}
	
	private Object createClientProxy(ClassLoader loader, 
		Class<?> []locatorClasses, EndpointReferenceType targetEPR, 
		ICallingContext callContext) throws ResourceException, GenesisIISecurityException
	{
		Class<?> []portTypes = ClientUtils.getLocatorPortTypes(locatorClasses);
		AxisClientInvocationHandler handler = new AxisClientInvocationHandler(
			locatorClasses, targetEPR, callContext);
		
		return Proxy.newProxyInstance(loader, portTypes, handler);
	}
	
	private AxisClientInvocationHandler 
		getInvocationHandler(Object clientProxy)
		throws ResourceException
	{
		InvocationHandler handler = Proxy.getInvocationHandler(clientProxy);
		if (handler == null)
			throw new ResourceException("Invalid client proxy.");
		if (!(handler instanceof AxisClientInvocationHandler))
			throw new ResourceException("Invalid client proxy.");
		return (AxisClientInvocationHandler)handler;
	}
	
	public EndpointReferenceType extractTargetEPR(Object proxy)
		throws ResourceException
	{
		AxisClientInvocationHandler handler = getInvocationHandler(proxy);
		return handler.getTargetEPR();
	}

	public Collection<GeniiAttachment> getAttachments(Object clientProxy)
		throws ResourceException
	{
		AxisClientInvocationHandler handler = getInvocationHandler(clientProxy);
		return handler.getInAttachments();
	}
	
	public GenesisIIEndpointInformation getLastEndpointInformation(
		Object clientProxy) throws ResourceException
	{
		AxisClientInvocationHandler handler = getInvocationHandler(clientProxy);
		return handler.getLastEndpointInformation();
	}

	public void setAttachments(Object clientProxy, 
		Collection<GeniiAttachment> attachments, 
		AttachmentType attachmentType) throws ResourceException
	{
		AxisClientInvocationHandler handler = getInvocationHandler(clientProxy);
		handler.setOutAttachments(attachments, attachmentType);
	}
	
	public void setTimeout(Object clientProxy,
		int timeoutMillis) throws ResourceException
	{
		AxisClientInvocationHandler handler = getInvocationHandler(clientProxy);
		handler.setTimeout(timeoutMillis);
	}
}
