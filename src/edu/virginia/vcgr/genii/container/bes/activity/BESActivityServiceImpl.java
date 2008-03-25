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
import java.util.Collection;
import java.util.HashMap;
import java.sql.SQLException;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;
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
import edu.virginia.vcgr.genii.client.bes.BESActivityConstants;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.context.*;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLInterpreter;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.bes.BES;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivityUtils.BESActivityInitInfo;
import edu.virginia.vcgr.genii.container.bes.activity.resource.IBESActivityResource;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec.SimpleExecutionPersonalityProvider;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec.SimpleExecutionUnderstanding;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.common.SByteIOFactory;
import edu.virginia.vcgr.genii.container.q2.QueueSecurity;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class BESActivityServiceImpl extends GenesisIIBase implements
		BESActivityPortType, BESActivityConstants
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(BESActivityServiceImpl.class);
	
	public BESActivityServiceImpl() throws RemoteException
	{
		super("BESActivityPortType");
		
		addImplementedPortType(GENII_BES_ACTIVITY_PORT_TYPE_QNAME);
		addImplementedPortType(
			WellKnownPortTypes.SBYTEIO_FACTORY_PORT_TYPE);
	}
	
	public QName getFinalWSResourceInterface()
	{
		return GENII_BES_ACTIVITY_PORT_TYPE_QNAME;
	}
	
	protected void setAttributeHandlers() throws NoSuchMethodException
	{
		super.setAttributeHandlers();
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
		
		String activityServiceName = "BESActivityPortType";
		Collection<Identity> owners = QueueSecurity.getCallerIdentities();
		
		try
		{
			JobDefinition_Type jsdl = initInfo.getJobDefinition();
			
			Object understanding = JSDLInterpreter.interpretJSDL(
				new SimpleExecutionPersonalityProvider(), jsdl);
			SimpleExecutionUnderstanding seUnderstanding =
				(SimpleExecutionUnderstanding)understanding;
				
			BES bes = BES.getBES(initInfo.getContainerID());
			if (bes == null)
				throw FaultManipulator.fillInFault(
					new ResourceUnknownFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription("Unknown BES \"" +
								initInfo.getContainerID() + "\".")
					}, null));
			
			bes.createActivity(
				resource.getKey().toString(), jsdl,	owners, 
				ContextManager.getCurrentContext(), 
				chooseDirectory(5), seUnderstanding.createExecutionPlan(),
				activityEPR, activityServiceName, seUnderstanding.getJobName());
		}
		catch (IOException fnfe)
		{
			throw new RemoteException("Unable to create new activity.", fnfe);
		}
		catch (ConfigurationException ce)
		{
			throw new RemoteException("Unable to create new activity.", ce);
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to create new activity.", sqe);
		}
		catch (JSDLException je)
		{
			throw new RemoteException("Unable to create new activity.", je);
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
			BESActivity activity = resource.findActivity(); 	
			ActivityState state = activity.getState();
			PrintStream ps = new PrintStream(out);
			ps.println("Status:");
			ps.println(state);
			
			if (state.isFailedState())
			{
				ps.print("\nFaults:");
				int lcv = 0;
				for (Throwable cause : activity.getFaults())
				{
					ps.println("\nFault #" + lcv);
					cause.printStackTrace(ps);
					ps.println();
					lcv++;
				}
			}
			
			ps.flush();
			return new OpenStreamResponse(factory.create());
		}
		catch (SQLException sqe)
		{
			throw FaultManipulator.fillInFault(
				new ResourceCreationFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription(sqe.getLocalizedMessage()) },
					null));
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
			Collection<Throwable> faults = resource.findActivity().getFaults();
			if (faults != null && faults.size() > 0)
			{
				Throwable cause = faults.iterator().next();
				if (cause != null)
					serializedFault = DBSerializer.serialize(cause);
			}
			
			return new BESActivityGetErrorResponseType(serializedFault);
		}
		catch (SQLException sqe)
		{
			throw FaultManipulator.fillInFault(
				new ResourceCreationFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription(sqe.getLocalizedMessage()) },
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
	}
}
