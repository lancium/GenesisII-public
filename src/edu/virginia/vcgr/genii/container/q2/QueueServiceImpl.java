package edu.virginia.vcgr.genii.container.q2;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStatusType;
import org.ggf.bes.factory.CreateActivityResponseType;
import org.ggf.bes.factory.CreateActivityType;
import org.ggf.bes.factory.DestroyActivitiesResponseType;
import org.ggf.bes.factory.DestroyActivitiesType;
import org.ggf.bes.factory.FreezeActivitiesResponseType;
import org.ggf.bes.factory.FreezeActivitiesType;
import org.ggf.bes.factory.GetActivityDocumentResponseType;
import org.ggf.bes.factory.GetActivityDocumentsResponseType;
import org.ggf.bes.factory.GetActivityDocumentsType;
import org.ggf.bes.factory.GetActivityStatusResponseType;
import org.ggf.bes.factory.GetActivityStatusesResponseType;
import org.ggf.bes.factory.GetActivityStatusesType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;
import org.ggf.bes.factory.GetStatePathsResponseType;
import org.ggf.bes.factory.GetStatePathsType;
import org.ggf.bes.factory.InvalidRequestMessageFaultType;
import org.ggf.bes.factory.NotAcceptingNewActivitiesFaultType;
import org.ggf.bes.factory.NotAuthorizedFaultType;
import org.ggf.bes.factory.PersistActivitiesResponseType;
import org.ggf.bes.factory.PersistActivitiesType;
import org.ggf.bes.factory.RestartActivitiesResponseType;
import org.ggf.bes.factory.RestartActivitiesType;
import org.ggf.bes.factory.TerminateActivitiesResponseType;
import org.ggf.bes.factory.TerminateActivitiesType;
import org.ggf.bes.factory.TerminateActivityResponseType;
import org.ggf.bes.factory.ThawActivitiesResponseType;
import org.ggf.bes.factory.ThawActivitiesType;
import org.ggf.bes.factory.UnknownActivityIdentifierFaultType;
import org.ggf.bes.factory.UnsupportedFeatureFaultType;
import org.ggf.bes.management.StartAcceptingNewActivitiesResponseType;
import org.ggf.bes.management.StartAcceptingNewActivitiesType;
import org.ggf.bes.management.StopAcceptingNewActivitiesResponseType;
import org.ggf.bes.management.StopAcceptingNewActivitiesType;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobMultiDefinition_Type;
import org.morgan.inject.MInject;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;
import org.xmlsoap.schemas.soap.envelope.Fault;

import edu.virginia.cs.vcgr.genii.job_management.ConfigureRequestType;
import edu.virginia.cs.vcgr.genii.job_management.GetJobLogRequest;
import edu.virginia.cs.vcgr.genii.job_management.GetJobLogResponse;
import edu.virginia.cs.vcgr.genii.job_management.IterateListResponseType;
import edu.virginia.cs.vcgr.genii.job_management.IterateStatusResponseType;
import edu.virginia.cs.vcgr.genii.job_management.JobErrorPacket;
import edu.virginia.cs.vcgr.genii.job_management.JobInformationType;
import edu.virginia.cs.vcgr.genii.job_management.QueryErrorRequest;
import edu.virginia.cs.vcgr.genii.job_management.ReducedJobInformationType;
import edu.virginia.cs.vcgr.genii.job_management.SubmitJobRequestType;
import edu.virginia.cs.vcgr.genii.job_management.SubmitJobResponseType;
import edu.virginia.vcgr.genii.algorithm.graph.GridDependency;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.ConstructionParametersType;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.queue.QueueConstants;
import edu.virginia.vcgr.genii.client.queue.QueueConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.ser.AnyHelper;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.BESActivityStateChangedContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.BESActivityTopics;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsRequestType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType;
import edu.virginia.vcgr.genii.container.bes.GeniiBESServiceImpl;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.iterator.FileOrDir;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorEntry;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorWrapper;
import edu.virginia.vcgr.genii.container.iterator.IteratorBuilder;
import edu.virginia.vcgr.genii.container.q2.forks.JobFork;
import edu.virginia.vcgr.genii.container.q2.forks.JobInformationFork;
import edu.virginia.vcgr.genii.container.q2.forks.RootRNSFork;
import edu.virginia.vcgr.genii.container.q2.iterator.QueueInMemoryIteratorEntry;
import edu.virginia.vcgr.genii.container.q2.resource.IQueueResource;
import edu.virginia.vcgr.genii.container.q2.resource.QueueDBResourceFactory;
import edu.virginia.vcgr.genii.container.q2.resource.QueueDBResourceProvider;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.ForkRoot;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkBaseService;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.queue.QueuePortType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;
import edu.virginia.vcgr.jsdl.JobDefinition;
import edu.virginia.vcgr.jsdl.sweep.SweepException;

/**
 * This is the service class that the container redirects SOAP messages to.
 * 
 * @author mmm2a
 */
@ForkRoot(RootRNSFork.class)
@GeniiServiceConfiguration(resourceProvider = QueueDBResourceProvider.class)
@ConstructionParametersType(QueueConstructionParameters.class)
@GridDependency(GeniiBESServiceImpl.class)
public class QueueServiceImpl extends ResourceForkBaseService implements QueuePortType
{
	static private Log _logger = LogFactory.getLog(QueueServiceImpl.class);
	
	// 2020-12-1 by ASG
	// keyInEPR is intended as a replacement for instanceof(GeniiNoOutcalls) which was a bit hacky.
	// If it is "true", we will not put key material in the X.509. This will in turn prevent delegation to instances
	// of a type that returns true, and will make transporting and storing EPR's consume MUCH less space.
	public boolean keyInEPR() {
		return true;
	}
	

	// static private final long _DEFAULT_TIME_TO_LIVE = 1000L * 60 * 60;
	static public QName _JOBID_QNAME = new QName(GenesisIIConstants.GENESISII_NS, "job-id");

	@MInject(lazy = true)
	private IQueueResource _resource;

	@MInject(injectionFactory = QueueManagerInjectionFactory.class)
	private QueueManager _queueMgr;

	public QueueServiceImpl() throws RemoteException
	{
		/* Indicate the port type name to the base class */
		super("QueuePortType");

		/*
		 * Now we have to add our own port types to the list of port types implemented by this service.
		 */
		addImplementedPortType(QueueConstants.QUEUE_PORT_TYPE());
	}

	@Override
	protected void postCreate(ResourceKey key, EndpointReferenceType newEPR, ConstructionParameters cParams,
		GenesisHashMap constructionParameters, Collection<MessageElement> resolverCreationParameters)
		throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(key, newEPR, cParams, constructionParameters, resolverCreationParameters);

		IQueueResource resource = (IQueueResource) key.dereference();
		resource.setEPR(newEPR);
	}

	public PortType getFinalWSResourceInterface()
	{
		return QueueConstants.QUEUE_PORT_TYPE();
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object completeJobs(String[] completeRequest) throws RemoteException
	{_logger.debug("Entering Qservice::completeJobs");
		try {
			_queueMgr.completeJobs(completeRequest);
			_logger.debug("Exiting Qservice::completeJobs");
			return null;
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to complete jobs in queue.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object rescheduleJobs(String[] jobs) throws RemoteException
	{
		try {
			_queueMgr.rescheduleJobs(jobs);
			return null;
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to reschedule jobs in queue.", sqe);
		}
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object resetJobs(String[] jobs) throws RemoteException
	{
		try {
			_queueMgr.resetJobs(jobs);
			return null;
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to reset jobs in queue.", sqe);
		}
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object persistJobs(String[] jobs) throws RemoteException
	{
		try {
			_queueMgr.persistJobs(jobs);
			return null;
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to persist jobs in queue.", sqe);
		}
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object restartJobs(String[] jobs) throws RemoteException
	{
		try {
			_queueMgr.restartJobs(jobs);
			return null;
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to restart jobs in queue.", sqe);
		}
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object freezeJobs(String[] jobs) throws RemoteException
	{
		try {
			_queueMgr.freezeJobs(jobs);
			return null;
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to freeze jobs in queue.", sqe);
		}
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object thawJobs(String[] jobs) throws RemoteException
	{
		try {
			_queueMgr.thawJobs(jobs);
			return null;
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to thaw jobs in queue.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public Object configureResource(ConfigureRequestType configureRequest) throws RemoteException
	{
		try {
			_queueMgr.configureBES(configureRequest.getQueueResource(), configureRequest.getNumSlots().intValue(),
				configureRequest.getNumCores().intValue());
			return null;
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to add bes container.", sqe);
		}
	}

	private QueueInMemoryIteratorEntry getIterableStatus(String[] getStatusRequest) throws RemoteException
	{
		_logger.debug("QService::Entering getIterableStatus");
		try {
			QueueInMemoryIteratorEntry qmie = _queueMgr.getIterableJobStatus(getStatusRequest);
			_logger.debug("QService::Exiting getIterableStatus");	return qmie;
		}

		catch (SQLException sqe) {
			_logger.debug("QService:: EXCEPTION Exiting getIterableStatus");	
			throw new RemoteException("Unable to list jobs in queue.", sqe);
		}

	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public IterateStatusResponseType iterateStatus(String[] iterateStatusRequest) throws RemoteException
	{
		_logger.debug("QService::Entering iterateStatus");
		Collection<MessageElement> col = new LinkedList<MessageElement>();
		QueueInMemoryIteratorEntry qmie = getIterableStatus(iterateStatusRequest);
		List<InMemoryIteratorEntry> indices = new LinkedList<InMemoryIteratorEntry>();

		for (JobInformationType jit : qmie.getReturnables())
			col.add(AnyHelper.toAny(jit));

		if (qmie.isIterable()) {
			for (String jobID : qmie.getIterableIDs()) {
				indices.add(new InMemoryIteratorEntry(null, jobID, true, FileOrDir.UNKNOWN));
			}
		}

		InMemoryIteratorWrapper imiw = new InMemoryIteratorWrapper(this.getClass().getName(), indices, new Object[] { _queueMgr });
		IteratorBuilder<MessageElement> builder = iteratorBuilder();
		builder.preferredBatchSize(QueueConstants.PREFERRED_BATCH_SIZE);
		builder.addElements(col);
		_logger.debug("QService::Exiting iterateStatus");

		return new IterateStatusResponseType(builder.create(imiw));
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public JobErrorPacket[] queryErrorInformation(QueryErrorRequest arg0) throws RemoteException
	{
		try {
			return _queueMgr.queryErrorInformation(arg0.getJobTicket());
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to complete jobs in queue.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetJobLogResponse getJobLog(GetJobLogRequest arg0) throws RemoteException
	{
		try {
			return _queueMgr.getJobLog(arg0.getJobTicket());
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to get job log endpoint.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object killJobs(String[] killRequest) throws RemoteException
	{
		try {
			_queueMgr.killJobs(killRequest);
			return null;
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to kill jobs in queue.", sqe);
		}
	}

	private ReducedJobInformationType[] listJobs(Object listRequest) throws RemoteException
	{
		Collection<ReducedJobInformationType> jobs;

		try {
			jobs = _queueMgr.listJobs(null);
			return jobs.toArray(new ReducedJobInformationType[0]);
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to list jobs in queue.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public IterateListResponseType iterateListJobs(Object iterateListRequest) throws RemoteException
	{
		Collection<MessageElement> col = new LinkedList<MessageElement>();

		for (ReducedJobInformationType rjit : listJobs(iterateListRequest))
			col.add(AnyHelper.toAny(rjit));

		IteratorBuilder<MessageElement> builder = iteratorBuilder();
		// 2020-10-26 by ASG changed from 100 to existing constant.
		builder.preferredBatchSize(QueueConstants.PREFERRED_BATCH_SIZE);
		builder.addElements(col);
		return new IterateListResponseType(builder.create());
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public IterateHistoryEventsResponseType iterateHistoryEvents(IterateHistoryEventsRequestType arg0) throws RemoteException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();

		if (arg0 != null && arg0.getResourceHint() != null) {
			try {
				_queueMgr.getJobStatus(new String[] { arg0.getResourceHint() });
			} catch (SQLException sqe) {
				throw new RemoteException("Unable to check that job exists.", sqe);
			}

			arg0.setResourceHint(new QueueDatabase(rKey.getResourceKey()).historyKey(arg0.getResourceHint()));
		}

		return super.iterateHistoryEvents(arg0);
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public SubmitJobResponseType submitJob(SubmitJobRequestType submitJobRequest) throws RemoteException
	{
		String ticket;

		try {
			if (!_resource.isAcceptingNewActivites())
				throw new RemoteException("Queue is not accepting activities at the moment!");

			JobDefinition jobDefinition = JSDLUtils.convert(submitJobRequest.getJobDefinition());
			// decide if the job has any parameter sweeps so we'll handle the sweeps properly.
			if (jobDefinition.parameterSweeps().size() > 0) {
				// create a sweeping job object and use specialized submit.
				SweepingJob sj = new SweepingJob(jobDefinition, submitJobRequest, _queueMgr);
				ticket = _queueMgr.submitJob(sj, submitJobRequest.getPriority(), submitJobRequest.getJobDefinition());
				sj.setOwnTicket(ticket);
			} else {
				// create a "normal" job and submit it.
				ticket = _queueMgr.submitJob(submitJobRequest.getPriority(), submitJobRequest.getJobDefinition());
			}

			return new SubmitJobResponseType(ticket);
		} catch (IOException ioe) {
			throw new RemoteException("Unable to submit job to queue.", ioe);
		} catch (SQLException sqe) {
			throw new RemoteException("Unable to submit job to queue.", sqe);
		} catch (JAXBException e) {
			throw new RemoteException("Unable to submit job to queue.", e);
		} catch (SweepException e) {
			throw new RemoteException("Unable to submit job to queue.", e);
		}
	}

	/*
	 * This method is called automatically when the Web server first comes up. We use it to restart the queue from where it left off.
	 */
	@Override
	public boolean startup()
	{
		boolean serviceCreated = super.startup();

		return serviceCreated;
	}

	@Override
	public void postStartup()
	{
		try {
			/*
			 * In order to make out calls, we have to have a working context so we go ahead and create an empty one.
			 */
			WorkingContext.setCurrentWorkingContext(new WorkingContext());

			/*
			 * Now we get the database connection pool configured with this service
			 */
			ServerDatabaseConnectionPool connectionPool =
				((QueueDBResourceFactory) ResourceManager.getServiceResource(_serviceName).getProvider().getFactory()).getConnectionPool();

			_logger.info("Restarting all BES Managers.");
			QueueManager.startAllManagers(connectionPool);
		} catch (Exception e) {
			_logger.error("Unable to start resource info managers.", e);
		} finally {
			WorkingContext.setCurrentWorkingContext(null);
			_logger.info("Done restarting all BES Managers.");
		}
	}

	@Override
	protected void preDestroy() throws RemoteException, ResourceException
	{
		super.preDestroy();

		try {
			_queueMgr.close();
		} catch (IOException ioe) {
			throw new ResourceException("Unable to pre-destroy queue.", ioe);
		}
	}

	@Override
	protected void registerNotificationHandlers(NotificationMultiplexer multiplexer)
	{
		super.registerNotificationHandlers(multiplexer);

		multiplexer.registerNotificationHandler(BESActivityTopics.ACTIVITY_STATE_CHANGED_TO_FINAL_TOPIC.asConcreteQueryExpression(),
			new LegacyBESActivityStateChangeFinalNotificationHandler());
	}

	private class LegacyBESActivityStateChangeFinalNotificationHandler extends AbstractNotificationHandler<BESActivityStateChangedContents>
	{
		private LegacyBESActivityStateChangeFinalNotificationHandler()
		{
			super(BESActivityStateChangedContents.class);
		}

		@Override
		public String handleNotification(TopicPath topic, EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference, BESActivityStateChangedContents contents) throws Exception
		{
			JobCompletedAdditionUserData userData = contents.additionalUserData(JobCompletedAdditionUserData.class);

			if (userData == null)
				throw new RemoteException("Missing required user data for notification");

			long jobid = userData.jobID();
			ActivityState state = contents.activityState();
			if (state.isFinalState()) {
				// 2021-04-08 by ASG, catch failed states and call failjob
				if (state.isFailedState()) {
					//_queueMgr.failjob(jobid, true);
				}
				else
					_queueMgr.cleanUpJob(jobid);

			}
			return NotificationConstants.OK;
		}
	}

	public void submitJobStream(InputStream in) throws IOException
	{
		if (!in.markSupported())
			throw new IOException("Can only submit jobs from streams that support marking.");

		in.mark(Integer.MAX_VALUE);

		if (!submitJobTrySingle(in)) {
			in.reset();
			submitJobTryMulti(in);
		}
	}

	private boolean submitJobTryMulti(InputStream in) throws IOException
	{
		JobDefinition_Type[] jobDefs =
			((JobMultiDefinition_Type) ObjectDeserializer.deserialize(new InputSource(in), JobMultiDefinition_Type.class)).getJobDefinition();
		if (jobDefs == null)
			return false;

		for (JobDefinition_Type jobDef : jobDefs)
			submitJob(new SubmitJobRequestType(jobDef, (byte) 0x0));

		return true;
	}

	private boolean submitJobTrySingle(InputStream in) throws IOException
	{
		JobDefinition_Type jobDef = (JobDefinition_Type) ObjectDeserializer.deserialize(new InputSource(in), JobDefinition_Type.class);
		if (jobDef == null || jobDef.getJobDescription() == null)
			return false;

		submitJob(new SubmitJobRequestType(jobDef, (byte) 0x0));
		return true;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public Object forceUpdate(String[] arg0) throws RemoteException
	{
		for (String arg : arg0) {
			try {
				_queueMgr.forceBESUpdate(arg);
			} catch (SQLException e) {
				_logger.error(String.format("Unable to force update for resource %s.", arg), e);
			}
		}

		return null;
	}

	static private String getJobTicketFromActivityEPR(EndpointReferenceType activityEPR) throws RemoteException
	{
		AddressingParameters addressingParameters = new AddressingParameters(activityEPR.getReferenceParameters());
		ResourceForkInformation rForkInfo = (ResourceForkInformation) addressingParameters.getResourceForkInformation();
		if (rForkInfo != null) {
			String forkPath = rForkInfo.forkPath();
			String jobTicket = JobInformationFork.determineJobTicketFromForkPath(forkPath);
			return jobTicket;
		}

		throw new RemoteException("Unable to find job ticket in activity EPR!");
	}

	private GetActivityStatusResponseType getActivityStatus(EndpointReferenceType activity)
	{
		ActivityStatusType activityStatus = null;
		Fault fault = null;

		try {
			String jobTicket = getJobTicketFromActivityEPR(activity);
			activityStatus = _queueMgr.getBESActivityStatus(jobTicket);
		} catch (Throwable cause) {
			fault = new Fault(new QName("http://tempuri.org", "fault"), cause.getLocalizedMessage(), null, null);
		}

		return new GetActivityStatusResponseType(activity, activityStatus, fault, null);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetActivityStatusesResponseType getActivityStatuses(GetActivityStatusesType parameters)
		throws RemoteException, UnknownActivityIdentifierFaultType
	{
		GetActivityStatusResponseType[] responses;
		EndpointReferenceType[] activities = parameters.getActivityIdentifier();
		responses = new GetActivityStatusResponseType[activities.length];

		for (int lcv = 0; lcv < activities.length; lcv++)
			responses[lcv] = getActivityStatus(activities[lcv]);

		return new GetActivityStatusesResponseType(responses, null);
	}

	private TerminateActivityResponseType terminateActivity(EndpointReferenceType activity)
	{
		boolean terminated = false;
		Fault fault = null;

		try {
			String jobTicket = getJobTicketFromActivityEPR(activity);
			killJobs(new String[] { jobTicket });
//			completeJobs(new String[] { jobTicket });
			terminated = true;
		} catch (Throwable cause) {
			fault = new Fault(new QName("http://tempuri.org", "fault"), cause.getLocalizedMessage(), null, null);
		}

		return new TerminateActivityResponseType(activity, terminated, fault, null);
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public TerminateActivitiesResponseType terminateActivities(TerminateActivitiesType parameters)
		throws RemoteException, UnknownActivityIdentifierFaultType
	{
		TerminateActivityResponseType[] responses;
		EndpointReferenceType[] activities = parameters.getActivityIdentifier();
		responses = new TerminateActivityResponseType[activities.length];

		for (int lcv = 0; lcv < activities.length; lcv++)
			responses[lcv] = terminateActivity(activities[lcv]);

		return new TerminateActivitiesResponseType(responses, null);
	}

	private GetActivityDocumentResponseType getActivityDocument(EndpointReferenceType activity)
	{
		JobDefinition_Type jobDef = null;
		Fault fault = null;

		try {
			String jobTicket = getJobTicketFromActivityEPR(activity);
			jobDef = _queueMgr.getJobDefinition(jobTicket);
		} catch (Throwable cause) {
			fault = new Fault(new QName("http://tempuri.org", "fault"), cause.getLocalizedMessage(), null, null);
		}

		return new GetActivityDocumentResponseType(activity, jobDef, fault, null);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetActivityDocumentsResponseType getActivityDocuments(GetActivityDocumentsType parameters)
		throws RemoteException, UnknownActivityIdentifierFaultType
	{
		GetActivityDocumentResponseType[] responses;
		EndpointReferenceType[] activities = parameters.getActivityIdentifier();
		responses = new GetActivityDocumentResponseType[activities.length];

		for (int lcv = 0; lcv < activities.length; lcv++)
			responses[lcv] = getActivityDocument(activities[lcv]);

		return new GetActivityDocumentsResponseType(responses, null);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetFactoryAttributesDocumentResponseType getFactoryAttributesDocument(GetFactoryAttributesDocumentType parameters)
		throws RemoteException
	{
		ConstructionParameters baseCP = _resource.constructionParameters(QueueConstructionParameters.class);

		if (baseCP == null || !(baseCP instanceof QueueConstructionParameters)) {
			// Default
			throw new RemoteException("The construction parameters for " + "this Queue have not been overridden.  It therefor "
				+ "cannot currently be used as a BES!");
		}

		QueueAsBESFactoryAttributesUtilities utils =
			new QueueAsBESFactoryAttributesUtilities(_queueMgr.getBESManager().allBESInformation(), (QueueConstructionParameters) baseCP);
		boolean isAcceptingNewActivities = _resource.isAcceptingNewActivites();
		long totalNumberOfActivities = _queueMgr.getJobCount();
		return new GetFactoryAttributesDocumentResponseType(
			utils.factoryResourceAttributes(isAcceptingNewActivities, totalNumberOfActivities), null);
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateActivityResponseType createActivity(CreateActivityType parameters) throws RemoteException,
		NotAcceptingNewActivitiesFaultType, InvalidRequestMessageFaultType, UnsupportedFeatureFaultType, NotAuthorizedFaultType
	{
		SubmitJobResponseType resp = submitJob(new SubmitJobRequestType(parameters.getActivityDocument().getJobDefinition(), (byte) 0));
		String jobTicket = resp.getJobTicket();
		String forkPath = String.format("/jobs/mine/all/%s", jobTicket);
		EndpointReferenceType target = createForkEPR(forkPath, new JobFork(this, forkPath).describe());
		return new CreateActivityResponseType(target, parameters.getActivityDocument(), null);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public StopAcceptingNewActivitiesResponseType stopAcceptingNewActivities(StopAcceptingNewActivitiesType parameters) throws RemoteException
	{
		_resource.isAcceptingNewActivites(false);
		return new StopAcceptingNewActivitiesResponseType();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public StartAcceptingNewActivitiesResponseType startAcceptingNewActivities(StartAcceptingNewActivitiesType parameters)
		throws RemoteException
	{
		_resource.isAcceptingNewActivites(true);
		return new StartAcceptingNewActivitiesResponseType();
	}

	/*
	 * Do not change the name or signature of the below method. It is used in WSIteratorDBResource using java-reflection.
	 * 
	 * If modifying: edit in WSIteratorDBResource.java and EnhancedRNSServiceImpl.java and LightWeightExportDirFork.java .
	 */

	public static MessageElement getIndexedContent(Connection connection, InMemoryIteratorEntry entry, Object[] queueManager,
		boolean shortForm) throws ResourceException
	{
		if (queueManager == null || entry == null || connection == null)
			throw new ResourceException("Unable to stat jobs in queue");

		if (queueManager[0] == null)
			throw new ResourceException("Unable to stat jobs in queue");

		QueueManager qMgr = (QueueManager) queueManager[0];
		JobInformationType jit;

		try {
			jit = qMgr.getStatusFromID(Long.parseLong(entry.getId()), connection);
		} catch (GenesisIISecurityException gse) {
			throw new ResourceException("Unable to stat jobs in queue.", gse);
		} catch (SQLException sqe) {
			throw new ResourceException("Unable to stat jobs in queue.", sqe);
		}
		return AnyHelper.toAny(jit);

	}
	
	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public PersistActivitiesResponseType persistActivities(PersistActivitiesType parameters)
		throws RemoteException, UnknownActivityIdentifierFaultType 
	{
		_logger.debug("persistActivities called on QueueServiceImpl. This is currently not supported. Ignoring request.");
		return null;
	}
	
	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public GetStatePathsResponseType getStatePaths(GetStatePathsType parameters)
		throws RemoteException, UnknownActivityIdentifierFaultType
	{
		_logger.debug("persistActivities called on QueueServiceImpl. This is currently not supported. Ignoring request.");
		return null;
	}
	
	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public RestartActivitiesResponseType restartActivities(RestartActivitiesType parameters)
		throws RemoteException, UnknownActivityIdentifierFaultType
	{
		_logger.debug("persistActivities called on QueueServiceImpl. This is currently not supported. Ignoring request.");
		return null;
	}
	
	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public FreezeActivitiesResponseType freezeActivities(FreezeActivitiesType parameters)
		throws RemoteException, UnknownActivityIdentifierFaultType
	{
		_logger.debug("freezeActivities called on QueueServiceImpl. This is currently not supported. Ignoring request.");
		return null;
	}
	
	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public ThawActivitiesResponseType thawActivities(ThawActivitiesType parameters)
		throws RemoteException, UnknownActivityIdentifierFaultType
	{
		_logger.debug("thawActivities called on QueueServiceImpl. This is currently not supported. Ignoring request.");
		return null;
	}
	
	//LAK 2020 Aug 13: This is a stub that should not be implemented in the queue.
	@Override
	public DestroyActivitiesResponseType destroyActivities(DestroyActivitiesType parameters)
			throws RemoteException, UnknownActivityIdentifierFaultType {
		// TODO Auto-generated method stub
		return null;
	}
}
