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
package edu.virginia.vcgr.genii.client.comm;

import java.util.Collection;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

public interface IProxyFactory
{
	public <IFace> IFace createProxy(ClassLoader loader,
		Class<IFace> iface, EndpointReferenceType epr, ICallingContext callContext)
			throws ResourceException, GenesisIISecurityException;
	
	/**
	 * This method can be used to determine the EPR that a proxy (created by this
	 * same class (not necessarily same instance)) points to.
	 * 
	 * @param proxy An object that was returned from the createClientProxy method
	 * above in this same class.
	 * @return The EndpointReferenceType that the given proxy targets.
	 */
	public EndpointReferenceType extractTargetEPR(Object proxy)
		throws ResourceException;
	
	public void setAttachments(Object clientProxy, 
		Collection<GeniiAttachment> attachments,
		AttachmentType attachmentType) throws ResourceException;
	public Collection<GeniiAttachment> getAttachments(Object clientProxy)
		throws ResourceException;
	
	public void setTimeout(Object clientProxy, int timeoutMillis) 
		throws ResourceException;
	
	public GenesisIIEndpointInformation getLastEndpointInformation(
		Object clientProxy) throws ResourceException;
}
