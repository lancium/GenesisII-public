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
package edu.virginia.vcgr.genii.container.context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.message.MessageElement;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.*;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.container.Container;

public class AxisBasedContextResolver implements IContextResolver
{
	
	@SuppressWarnings("unchecked")
	public ICallingContext load() throws ResourceException, IOException,
			FileNotFoundException
	{
		WorkingContext workingContext = WorkingContext.getCurrentWorkingContext();
		
		ICallingContext retval;
		if ((retval = (ICallingContext) workingContext.getProperty(WorkingContext.CURRENT_CONTEXT_KEY)) != null) {
			return retval;
		}

		MessageContext messageContext = 
			(MessageContext) workingContext.getProperty(WorkingContext.MESSAGE_CONTEXT_KEY);
		ContextType ct = null;
		
		if (messageContext != null) {
			try {
				SOAPMessage m = messageContext.getMessage();
				SOAPHeader header = m.getSOAPHeader();
	
				Iterator<? extends SOAPHeaderElement> iter = header.examineAllHeaderElements();
				while (iter.hasNext()) {
					SOAPHeaderElement he = (SOAPHeaderElement) iter.next();
					QName heName = new QName(he.getNamespaceURI(), he.getLocalName());
					if (heName.equals(GenesisIIConstants.CONTEXT_INFORMATION_QNAME)) {
						Element em = ((MessageElement) he).getRealElement();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						PrintStream ps = new PrintStream(baos);
						ps.println(em);
						ps.close();
						ByteArrayInputStream bais = new ByteArrayInputStream(baos
								.toByteArray());
						ct = (ContextType) ObjectDeserializer.deserialize(
								new InputSource(bais), ContextType.class);
						break;
					}
				}
			} catch (SOAPException se) {
				throw new AxisFault(se.getLocalizedMessage(), se);
			} catch (IOException e) {
				throw new AuthZSecurityException(e.getMessage(), e);
			}
		}
			
		IResource resource = ResourceManager.getCurrentResource().dereference();
		CallingContextImpl resourceContext = 
			(CallingContextImpl) resource.getProperty(
				IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME);

		if (resourceContext == null) {
			retval = new CallingContextImpl(ct);
		} else {
			retval = resourceContext.deriveNewContext(ct);
		}
		
		// MOOCH: add comments 
		ArrayList<GamlCredential> callerCredentials = new ArrayList<GamlCredential>();
		ArrayList<Serializable> signedAssertions = 
			retval.getProperty(GamlCredential.ENCODED_GAML_CREDENTIALS_PROPERTY);
		retval.removeProperty(GamlCredential.ENCODED_GAML_CREDENTIALS_PROPERTY);
		TransientCredentials transientCredentials = 
			TransientCredentials.getTransientCredentials(retval); 
		if (signedAssertions != null) {
			Iterator<Serializable> itr = signedAssertions.iterator();
			while (itr.hasNext()) {
				GamlCredential signedAssertion = (GamlCredential) itr.next();
				transientCredentials._credentials.add(signedAssertion);
				callerCredentials.add(signedAssertion);
			}
		}
		retval.setTransientProperty(GamlCredential.CALLER_CREDENTIALS_PROPERTY, callerCredentials);
		
		
		// place the resource's key material in the transient calling context
		// so that it may be properly used for outgoing messages
		try {
			Certificate[] targetCertChain = (Certificate[]) resource
					.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
			if ((targetCertChain != null) && (targetCertChain.length > 0)) {
				retval.setActiveKeyAndCertMaterial(
						new KeyAndCertMaterial((X509Certificate[]) targetCertChain, 
								Container.getContainerPrivateKey()));
			}
		} catch (GeneralSecurityException e) {
			throw new AuthZSecurityException(e.getMessage(), e);
		}
		
		
		
		workingContext.setProperty(WorkingContext.CURRENT_CONTEXT_KEY, retval);

		
		return retval;
	}

	public void store(ICallingContext ctxt) throws ResourceException, IOException
	{
		ResourceManager.getCurrentResource().dereference().setProperty(
			IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME, ctxt);
	}
}
