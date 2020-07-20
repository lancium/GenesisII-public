/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.genii.container.bes.activity;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Vector;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;
import org.ggf.jsdl.JobIdentification_Type;
import org.morgan.inject.MInject;
import org.morgan.util.GUID;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsn.base.Subscribe;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.activity.BESActivityGetErrorResponseType;
import edu.virginia.vcgr.genii.bes.activity.BESActivityPortType;
import edu.virginia.vcgr.genii.client.bes.BESActivityConstants;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.ExecutionPhase;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.ConstructionParametersType;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLInterpreter;
import edu.virginia.vcgr.genii.client.jsdl.JobRequest;
import edu.virginia.vcgr.genii.client.jsdl.parser.ExecutionProvider;
import edu.virginia.vcgr.genii.client.jsdl.personality.PersonalityProvider;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.ExecutionUnderstanding;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscribeRequest;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.BESActivityTopics;
import edu.virginia.vcgr.genii.cloud.CloudConfiguration;
import edu.virginia.vcgr.genii.cloud.CloudJobWrapper;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.bes.BES;
import edu.virginia.vcgr.genii.container.bes.BESUtilities;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivityUtils.BESActivityInitInfo;
import edu.virginia.vcgr.genii.container.bes.activity.forks.RootRNSFork;
import edu.virginia.vcgr.genii.container.bes.activity.resource.BESActivityDBResourceProvider;
import edu.virginia.vcgr.genii.container.bes.activity.resource.IBESActivityResource;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.jsdl.personality.forkexec.ForkExecPersonalityProvider;
import edu.virginia.vcgr.genii.container.jsdl.personality.qsub.QSubPersonalityProvider;
import edu.virginia.vcgr.genii.container.q2.QueueSecurity;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.ForkRoot;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkBaseService;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.identity.Identity;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@ForkRoot(RootRNSFork.class)
@ConstructionParametersType(BESConstructionParameters.class)
@GeniiServiceConfiguration(resourceProvider = BESActivityDBResourceProvider.class)
public class BESActivityServiceImpl extends ResourceForkBaseService implements BESActivityPortType, BESActivityTopics
{
	static private Log _logger = LogFactory.getLog(BESActivityServiceImpl.class);

	// One week of life
	static private final long BES_ACTIVITY_LIFETIME = 1000L * 60 * 60 * 24 * 7 * 4;
	private BESActivityConstants bconsts = new BESActivityConstants();

	@MInject(lazy = true)
	private IBESActivityResource _resource;

//	private JSDLFileSystem filesystem;

	public BESActivityServiceImpl() throws RemoteException
	{
		super("BESActivityPortType");

		addImplementedPortType(bconsts.GENII_BES_ACTIVITY_PORT_TYPE());
	}

	public PortType getFinalWSResourceInterface()
	{
		return bconsts.GENII_BES_ACTIVITY_PORT_TYPE();
	}

	@Override
	protected void postCreate(ResourceKey rKey, EndpointReferenceType activityEPR, ConstructionParameters cParams,
		GenesisHashMap creationParameters, Collection<MessageElement> resolverCreationParams)
		throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, activityEPR, cParams, creationParameters, resolverCreationParams);

		if (_logger.isDebugEnabled())
			_logger.debug(String.format("Post creating a BES Activity with resource key \"%s\".", rKey.getResourceKey()));

		BESActivityInitInfo initInfo = BESActivityUtils.extractCreationProperties(creationParameters);

		Subscribe subscribe = initInfo.getSubscribeRequest();
		if (subscribe != null){
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Subscribe request for resource key \"%s\"." + subscribe.toString(), rKey.getResourceKey()));
			processSubscribeRequest((String) _resource.getKey(), new SubscribeRequest(subscribe));
		}
		String activityServiceName = "BESActivityPortType";
		Collection<Identity> owners = QueueSecurity.getCallerIdentities(true);
		
		// 2020-07-17 by ASG. This block of code moved here from further below so that the _ipaddr can be acquired.
		BES bes = null;
		try {
			bes = BES.getBES(initInfo.getContainerID());
			WSName wsname = new WSName(activityEPR);
			if (wsname.isValidWSName())
				_logger
					.info(String.format("The EPI %s corresponds to activity id %s.", wsname.getEndpointIdentifier(), _resource.getKey()));
		} catch (IllegalStateException e) {
			_logger.error("caught illegal state exception trying to get BES information on container " + initInfo.getContainerID());
		}
		if (bes == null) {
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(null, null, null, null,
				new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Unknown BES \"" + initInfo.getContainerID() + "\".") },
				null));
		}
		// End of moved block of code.
		

		BESWorkingDirectory workingDirectory =
			new BESWorkingDirectory(chooseDirectory((BESConstructionParameters) _resource.constructionParameters(getClass()), 5), true);

		FilesystemManager fsManager = new FilesystemManager();
		fsManager.setWorkingDirectory(workingDirectory.getWorkingDirectory());
		String jobAnnotation=null;
		try {
			JobDefinition_Type jsdl = initInfo.getJobDefinition();
			String jobName;
			Vector<ExecutionPhase> executionPlan;

			CloudConfiguration cConfig = ((BESConstructionParameters) cParams).getCloudConfiguration();

			ExecutionUnderstanding executionUnderstanding;
		
			if (cConfig != null) {
				PersonalityProvider provider = new ExecutionProvider();
				JobRequest tJob = (JobRequest) JSDLInterpreter.interpretJSDL(provider, jsdl);
				executionPlan = CloudJobWrapper.createExecutionPlan(_resource.getKey().toString(), initInfo.getContainerID(), tJob,
					((BESConstructionParameters) cParams));
				jobName = tJob.getJobName();
			} else {

				NativeQueueConfiguration qConf = ((BESConstructionParameters) cParams).getNativeQueueConfiguration();

				if (qConf != null) {
					Object understanding = JSDLInterpreter.interpretJSDL(new QSubPersonalityProvider(fsManager, workingDirectory), jsdl);
					executionUnderstanding = (ExecutionUnderstanding) understanding;
				} else {
					Object understanding = JSDLInterpreter.interpretJSDL(new ForkExecPersonalityProvider(fsManager, workingDirectory), jsdl);
					executionUnderstanding = (ExecutionUnderstanding) understanding;
				}

				executionPlan = executionUnderstanding.createExecutionPlan((BESConstructionParameters) cParams, jsdl,bes.getBESipaddr());
				jobName = executionUnderstanding.getJobName();
				
			}

			_resource.setProperty(IBESActivityResource.FILESYSTEM_MANAGER, fsManager);

			// block of code getting the BES of the activity moved above.
			bes.createActivity(_resource.getConnection(), _resource.getKey().toString(), jsdl, owners, ContextManager.getExistingContext(),
				workingDirectory, executionPlan, activityEPR, activityServiceName, jobName, "undefined");


			if (_logger.isTraceEnabled()) {
				_logger.debug("after creating job, context has these creds:\n"
					+ TransientCredentials.getTransientCredentials(ContextManager.getExistingContext()).toString());
			}

			Calendar future = Calendar.getInstance();
			future.setTimeInMillis(System.currentTimeMillis() + BES_ACTIVITY_LIFETIME);
			_logger.debug(String.format("Setting term. time for BES Activity with resource key \"%s\".", rKey.getResourceKey()));
			setScheduledTerminationTime(future, rKey);
		} catch (IOException fnfe) {
			throw new RemoteException("Unable to create new activity.", fnfe);
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to create new activity.", sqe);
		} catch (JSDLException je) {
			throw new RemoteException("Unable to create new activity.", je);
		}
	}

	static public File getCommonDirectory(BESConstructionParameters creationProperties)
	{
		File basedir = null;

		if (creationProperties != null) {
			NativeQueueConfiguration qConf = creationProperties.getNativeQueueConfiguration();
			if (qConf != null) {
				File dir = qConf.sharedDirectory();
				if (dir != null)
					basedir = dir;
			}
		}

		File configDir = null;
		if (basedir == null) {
			configDir = BESUtilities.getBESWorkerDir();
		} else
			configDir = basedir;

		return configDir;
	}

	static private File chooseDirectory(BESConstructionParameters constructionParameters, int attempts) throws ResourceException
	{
		return new File(getCommonDirectory(constructionParameters), new GUID().toString());
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public BESActivityGetErrorResponseType getError(Object BESActivityGetErrorRequest) throws RemoteException, ResourceUnknownFaultType
	{
		try {
			byte[] serializedFault = null;
			Collection<Throwable> faults = _resource.findActivity().getFaults();
			if (faults != null && faults.size() > 0) {
				Throwable cause = faults.iterator().next();
				if (cause != null)
					serializedFault = DBSerializer.serialize(cause, Long.MAX_VALUE);
			}

			return new BESActivityGetErrorResponseType(serializedFault);
		} catch (SQLException sqe) {
			throw FaultManipulator.fillInFault(new ResourceCreationFaultType(null, null, null, null,
				new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(sqe.getLocalizedMessage()) }, null));
		} catch (IOException ioe) {
			throw FaultManipulator.fillInFault(new ResourceCreationFaultType(null, null, null, null,
				new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(ioe.getLocalizedMessage()) }, null));
		}
	}
}
