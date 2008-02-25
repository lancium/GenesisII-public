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
package edu.virginia.vcgr.genii.container.bes.activity;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.security.*;

import javax.xml.namespace.QName;

import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.GuaranteedDirectory;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.activity.BESActivityGetErrorResponseType;
import edu.virginia.vcgr.genii.bes.activity.BESActivityPortType;
import edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.context.*;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.security.gamlauthz.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.*;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.*;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivityUtils.BESActivityInitInfo;
import edu.virginia.vcgr.genii.container.bes.activity.resource.IBESActivityResource;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.common.SByteIOFactory;
import edu.virginia.vcgr.genii.container.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class BESActivityServiceImpl extends GenesisIIBase implements
		BESActivityPortType
{
	public BESActivityServiceImpl() throws RemoteException
	{
		super("BESActivityPortType");
		
		addImplementedPortType(
			WellKnownPortTypes.VCGR_BES_ACTIVITY_SERVICE_PORT_TYPE);
		addImplementedPortType(
			WellKnownPortTypes.SBYTEIO_FACTORY_PORT_TYPE);
	}
	
	public QName getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.VCGR_BES_ACTIVITY_SERVICE_PORT_TYPE;
	}
	
	protected void setAttributeHandlers() throws NoSuchMethodException
	{
		super.setAttributeHandlers();
		
		new BESActivityAttributeHandler(getAttributePackage());
	}
	
	@SuppressWarnings("unchecked")
	protected void postCreate(ResourceKey rKey,
		EndpointReferenceType activityEPR, HashMap<QName, Object> creationParameters)
		throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, activityEPR, creationParameters);
		
		IBESActivityResource resource = (IBESActivityResource)rKey.dereference();
		BESActivityInitInfo initInfo = BESActivityUtils.extractCreationProperties(
			creationParameters);
	
		try
		{
			resource.associateWithContainer(activityEPR, initInfo.getContainerID());
				
			ICallingContext ctxt = ContextManager.getCurrentContext(false);
			if (ctxt != null) {
				
				// delegate credentials inbound for me to the child resource so
				// when he comes alive he can use them because they're his

				X509Certificate[] activityCertChain = 
					(X509Certificate[])resource.getProperty(
							IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
				
				ArrayList<GamlCredential> credsToDelegate = 
					TransientCredentials.getTransientCredentials(ctxt)._credentials;
				ArrayList<GamlCredential> callerCredentials = (ArrayList<GamlCredential>)
					ctxt.getTransientProperty(GamlCredential.CALLER_CREDENTIALS_PROPERTY);
				for (GamlCredential cred : callerCredentials) {
					
					if (cred instanceof UsernameTokenIdentity) {
						// Do nothing: do not serialize UT tokens: we will be sending
						// it in its own header the old fashioned way.
						credsToDelegate.add(cred);
						
					} else if (cred instanceof SignedAssertion) {
						
						// Because we know an identity for the remote resource, delegate the 
						// current credential assertion to it
						DelegatedAttribute delegatedAttribute = new DelegatedAttribute(
								null,
								(SignedAssertion) cred, 
								activityCertChain);
						
						credsToDelegate.add(SignedCredentialCache.getCachedDelegateAssertion(
								delegatedAttribute, 
								Container.getContainerPrivateKey()));
					}

				}

				// have the activity context inherit the current path from 
				// the calling context
				resource.setProperty(
						IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME,
						ctxt);
			}
				
			
			resource.createProcess(activityEPR, chooseDirectory(10), 
					initInfo.getJobDefinition());
		}
		catch (JSDLException je)
		{
			throw FaultManipulator.fillInFault(
				new BaseFaultType(null, null, null, null,
					new BaseFaultTypeDescription[]
                     {
						new BaseFaultTypeDescription(je.getLocalizedMessage())
                     }, null));
		}
		catch (IOException ioe)
		{
			throw FaultManipulator.fillInFault(
				new BaseFaultType(null, null, null, null,
					new BaseFaultTypeDescription[]
                     {
						new BaseFaultTypeDescription(ioe.getLocalizedMessage())
                     }, null));
		}
		catch (ConfigurationException ce)
		{
			throw FaultManipulator.fillInFault(
				new BaseFaultType(null, null, null, null,
					new BaseFaultTypeDescription[]
                     {
						new BaseFaultTypeDescription(ce.getLocalizedMessage())
                     }, null));
		}
		catch (GeneralSecurityException ce)
		{
			throw FaultManipulator.fillInFault(
				new BaseFaultType(null, null, null, null,
					new BaseFaultTypeDescription[]
                     {
						new BaseFaultTypeDescription(ce.getLocalizedMessage())
                     }, null));
		}
	}
	
	static private File chooseDirectory(int attempts) throws ResourceException
	{
		try
		{
			File configDir = ConfigurationManager.getCurrentConfiguration().getUserDirectory();
			configDir = new GuaranteedDirectory(configDir, "bes-activities");
			
			for (int lcv = 0; lcv < attempts; lcv++)
			{
				File ret = new File(configDir, new GUID().toString());
				if (!ret.exists())
				{
					if (ret.mkdir())
						return ret;
				}
			}
			
			throw new ResourceException("Unable to create working directory.");
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.getMessage(), ioe);
		}
	}

	@RWXMapping(RWXCategory.READ)
	public OpenStreamResponse openStream(Object openStreamRequest) 
		throws RemoteException, ResourceCreationFaultType, ResourceUnknownFaultType
	{
		SByteIOFactory factory = null;
		
		IBESActivityResource resource = 
			(IBESActivityResource)ResourceManager.getCurrentResource().dereference();
		
		try
		{
			factory = createStreamableByteIOResource();
			OutputStream out = factory.getCreationStream();
			ActivityState state = ActivityState.fromActivityStatus(
				resource.getOverallStatus());
			PrintStream ps = new PrintStream(out);
			ps.println("Status:");
			ps.println(state);
			ps.flush();
			return new OpenStreamResponse(factory.create());
		}
		catch (ConfigurationException ce)
		{
			throw FaultManipulator.fillInFault(
				new ResourceCreationFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription(ce.getLocalizedMessage()) },
					null));
		}
		catch (IOException ioe)
		{
			throw FaultManipulator.fillInFault(
				new ResourceCreationFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription(ioe.getLocalizedMessage()) },
					null));
		}
		finally
		{
			StreamUtils.close(factory);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public BESActivityGetErrorResponseType getError(
			Object BESActivityGetErrorRequest) throws RemoteException,
			ResourceUnknownFaultType
	{
		try
		{
			byte []serializedFault = null;
			IBESActivityResource resource = 
				(IBESActivityResource)ResourceManager.getCurrentResource().dereference();
			Throwable cause = (Throwable)resource.getProperty(IBESActivityResource.ERROR_PROPERTY);
			if (cause != null)
				serializedFault = DBSerializer.serialize(cause);
			
			return new BESActivityGetErrorResponseType(serializedFault);
		}
		catch (IOException ioe)
		{
			throw FaultManipulator.fillInFault(
				new ResourceCreationFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription(ioe.getLocalizedMessage()) },
					null));
		}
	}
}
