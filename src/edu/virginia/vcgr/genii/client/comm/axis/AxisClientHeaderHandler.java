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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSEncryptionPart;
import org.morgan.util.GUID;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.appmgr.launcher.ApplicationLauncher;
import edu.virginia.vcgr.appmgr.launcher.ApplicationLauncherConsole;
import edu.virginia.vcgr.appmgr.version.Version;
import edu.virginia.vcgr.genii.client.ClientIdGenerator;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.CommConstants;
import edu.virginia.vcgr.genii.client.comm.GeniiSOAPHeaderConstants;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.comm.axis.security.MessageSecurity;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.context.ContextType;

public class AxisClientHeaderHandler extends BasicHandler
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(
		AxisClientHeaderHandler.class);
	
	private void setGenesisIIHeaders(MessageContext msgContext)
		throws AxisFault
	{
		SOAPHeader header;
		
		try
		{
			header = msgContext.getMessage().getSOAPHeader();
		
			Version currentVersion;
			ApplicationLauncherConsole console = ApplicationLauncher.getConsole();
			if (console != null)
			{
				currentVersion = console.currentVersion();
				
				if (currentVersion != null && 
					!currentVersion.equals(Version.EMPTY_VERSION))
				{
					SOAPHeaderElement geniiVersion = new SOAPHeaderElement(
							GeniiSOAPHeaderConstants.GENII_ENDPOINT_VERSION,
							currentVersion.toString());
					geniiVersion.setActor(null);
					geniiVersion.setMustUnderstand(false);
					header.addChildElement(geniiVersion);
				}
			}
			
			SOAPHeaderElement isGenesisII = new SOAPHeaderElement(
				GeniiSOAPHeaderConstants.GENII_ENDPOINT_QNAME,
				Boolean.TRUE);
			isGenesisII.setActor(null);
			isGenesisII.setMustUnderstand(false);
			header.addChildElement(isGenesisII);
		}
		catch (SOAPException se)
		{
			throw new AxisFault(se.getLocalizedMessage());
		}
	}
	
	private void setMessageID(MessageContext msgContext) throws AxisFault
	{
		SOAPHeaderElement messageid = new SOAPHeaderElement(
			new QName(
				EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI(),
				"MessageID"), "urn:uuid:" + new GUID());
		messageid.setActor(null);
		messageid.setMustUnderstand(false);
		try
		{
			msgContext.getMessage().getSOAPHeader().addChildElement(messageid);
		}
		catch (SOAPException se)
		{
			throw new AxisFault(se.getLocalizedMessage());
		}
	}
	
	private void setSOAPAction(MessageContext msgContext) throws AxisFault
	{
		String uri = msgContext.getSOAPActionURI();
		
		if ((uri != null) && (uri.length() > 0))
		{
			SOAPHeaderElement action = new SOAPHeaderElement(
				new QName(
					EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI(),
					"Action"), uri);
			action.setActor(null);
			action.setMustUnderstand(false);
			try
			{
				msgContext.getMessage().getSOAPHeader().addChildElement(action);
			}
			catch (SOAPException se)
			{
				throw new AxisFault(se.getLocalizedMessage());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setWSAddressingHeaders(MessageContext msgContext) throws AxisFault
	{
		if (!msgContext.containsProperty(CommConstants.TARGET_EPR_PROPERTY_NAME))
			return;
		
		EndpointReferenceType target = (EndpointReferenceType)msgContext.getProperty(
			CommConstants.TARGET_EPR_PROPERTY_NAME);
		if (target == null)
			return;
		
		try
		{
			String WSA_NS = EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI();
			SOAPHeader header = msgContext.getMessage().getSOAPHeader();
			
			SOAPHeaderElement to = new SOAPHeaderElement(
				new QName(WSA_NS, "To"), target.getAddress().get_value().toString());
			header.addChildElement(to);
			
			// specify that we need to sign the To header
			ArrayList<WSEncryptionPart> signParts = 
				(ArrayList<WSEncryptionPart>) msgContext.getProperty(
					CommConstants.MESSAGE_SEC_SIGN_PARTS);
			if (signParts == null) {
				signParts = new ArrayList<WSEncryptionPart>();
				msgContext.setProperty(CommConstants.MESSAGE_SEC_SIGN_PARTS, signParts);
			}
			signParts.add(new WSEncryptionPart(
	    			"To", 
	    			"http://www.w3.org/2005/08/addressing", 
	    			"Element"));
			
			
			ReferenceParametersType rpt = target.getReferenceParameters();
			if (rpt != null)
			{
				MessageElement []any = rpt.get_any();
				if (any != null)
				{
					Collection<QName> referenceParameters = new ArrayList<QName>(any.length);
					
					for (MessageElement elem : any)
					{
						SOAPHeaderElement she = new SOAPHeaderElement(elem);
						
						// dgm4d: Haxx for problem where resource keys go missing:
						// Basically we have resource keys occasionally set 
						// as MessageElement.objectValue, which isn't deep-copied
						// from "elem" during the SOAPHeaderElement construction.
						if ((elem.getObjectValue() != null) && 
							((she.getChildren() == null) || (she.getChildren().isEmpty())))
						{
							she.setObjectValue(elem.getObjectValue()); 
						}
						
						she.removeAttributeNS(
							EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI(),
							"IsReferenceParameter");
						she.addAttribute(
							EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI(),
							"IsReferenceParameter", "true");
						header.addChildElement(she);
						referenceParameters.add(elem.getQName());
					}
					
					for (QName refParamName : referenceParameters) 
					{
						// specify that we need to sign the reference params
						signParts.add(new WSEncryptionPart(
							refParamName.getLocalPart(),
							refParamName.getNamespaceURI(),
			    			"Element"));
					}
				}
			}
		}
		catch (SOAPException se)
		{
			throw new AxisFault(se.getLocalizedMessage(), se);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setCallingContextHeaders(MessageContext msgContext) throws AxisFault
	{
		ICallingContext callContext = null;
		if (msgContext.containsProperty(CommConstants.CALLING_CONTEXT_PROPERTY_NAME))
		{
			callContext = (ICallingContext)msgContext.getProperty(
			CommConstants.CALLING_CONTEXT_PROPERTY_NAME);
		}
		
		
		if (callContext == null) {
			try {
				callContext = new CallingContextImpl(new ContextType());
			} catch (IOException e) {
				throw new AxisFault(e.getLocalizedMessage(), e);
			}
		} else {
			// update any stale creds
			try { 
				ClientUtils.checkAndRenewCredentials(callContext, new Date(),
					new SecurityUpdateResults()); 
			} catch (GeneralSecurityException e) {
				throw new GenesisIISecurityException("Could not prepare outgoing calling context: " + e.getMessage(), e);
			}	

			// create a new derived calling context that is transient per 
			// the lifetime of this call
			callContext = callContext.deriveNewContext();
		}
		
		// process the transient credentials to prepare
		// the serializable portion of the calling context for them
		MessageSecurity msgSecData = 
			(MessageSecurity) msgContext.getProperty(CommConstants.MESSAGE_SEC_CALL_DATA);

		// Prepare outgoing GIICredentials contained within the  
		// calling-context's TransientCredentials, performing pre-delegation
		// and serialization steps.   
		MessageSecurity.messageSendPrepareHandler(
				callContext, 
				msgContext.getOperation().getMethod(), 
				msgSecData);
		
		try
		{
			if (_logger.isDebugEnabled())
			{
				_logger.debug(String.format("Calling Context:\n%s",
					callContext.describe()));
			}
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to log calling context information.", cause);
		}
		
		try
		{
			SOAPHeader header = msgContext.getMessage().getSOAPHeader();
			SOAPHeaderElement context = new SOAPHeaderElement(
				ObjectSerializer.toElement(callContext.getSerialized(),
					GenesisIIConstants.CONTEXT_INFORMATION_QNAME));
			header.addChildElement(context);
			
			// specify that we need to sign the calling context
			ArrayList<WSEncryptionPart> signParts = 
				(ArrayList<WSEncryptionPart>) msgContext.getProperty(
					CommConstants.MESSAGE_SEC_SIGN_PARTS);
			if (signParts == null) {
				signParts = new ArrayList<WSEncryptionPart>();
				msgContext.setProperty(CommConstants.MESSAGE_SEC_SIGN_PARTS, signParts);
			}
			signParts.add(new WSEncryptionPart(
    			"calling-context", 
    			"http://vcgr.cs.virginia.edu/Genesis-II", 
    			"Element"));
			
		} catch (IOException e) {
			throw new AxisFault(e.getLocalizedMessage(), e);
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage(), se);
		}
	}
	
	private void setClientID(MessageContext msgContext) throws AxisFault {
		SOAPHeaderElement clientId = new SOAPHeaderElement(
				GenesisIIConstants.CLIENT_ID_QNAME, ClientIdGenerator.getClientId());
		clientId.setActor(null);
		clientId.setMustUnderstand(false);
		try {
			msgContext.getMessage().getSOAPHeader().addChildElement(clientId);
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage());
		}
	}
	
	public void invoke(MessageContext msgContext) throws AxisFault
	{
		setMessageID(msgContext);
		setSOAPAction(msgContext);
		setWSAddressingHeaders(msgContext);
		setCallingContextHeaders(msgContext);
		setGenesisIIHeaders(msgContext);
		setClientID(msgContext);
	}
}