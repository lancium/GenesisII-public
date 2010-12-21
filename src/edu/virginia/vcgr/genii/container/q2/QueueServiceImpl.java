package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobMultiDefinition_Type;
import org.morgan.inject.MInject;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.queue.QueueConstants;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.AnyHelper;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.BESActivityStateChangedContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.BESActivityTopics;

import org.oasis_open.wsrf.basefaults.BaseFaultType;

import edu.virginia.vcgr.genii.common.IterateHistoryEventsRequestType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.q2.forks.RootRNSFork;
import edu.virginia.vcgr.genii.container.q2.resource.IQueueResource;
import edu.virginia.vcgr.genii.container.q2.resource.QueueDBResourceFactory;
import edu.virginia.vcgr.genii.container.q2.resource.QueueDBResourceProvider;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.ForkRoot;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkBaseService;
import edu.virginia.vcgr.genii.queue.ConfigureRequestType;
import edu.virginia.vcgr.genii.queue.GetJobLogRequest;
import edu.virginia.vcgr.genii.queue.GetJobLogResponse;
import edu.virginia.vcgr.genii.queue.IterateListResponseType;
import edu.virginia.vcgr.genii.queue.IterateStatusResponseType;
import edu.virginia.vcgr.genii.queue.JobErrorPacket;
import edu.virginia.vcgr.genii.queue.JobInformationType;
import edu.virginia.vcgr.genii.queue.QueryErrorRequest;
import edu.virginia.vcgr.genii.queue.QueuePortType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;
import edu.virginia.vcgr.genii.queue.SubmitJobRequestType;
import edu.virginia.vcgr.genii.queue.SubmitJobResponseType;
import edu.virginia.vcgr.jsdl.JobDefinition;
import edu.virginia.vcgr.jsdl.sweep.SweepException;
import edu.virginia.vcgr.jsdl.sweep.SweepListener;
import edu.virginia.vcgr.jsdl.sweep.SweepToken;
import edu.virginia.vcgr.jsdl.sweep.SweepUtility;

/**
 * This is the service class that the container redirects SOAP messages to.
 * 
 * @author mmm2a
 */
@ForkRoot(RootRNSFork.class)
@GeniiServiceConfiguration(
	resourceProvider=QueueDBResourceProvider.class)
public class QueueServiceImpl extends ResourceForkBaseService
	implements QueuePortType
{
	static private Log _logger = LogFactory.getLog(QueueServiceImpl.class);
	
	//static private final long _DEFAULT_TIME_TO_LIVE = 1000L * 60 * 60;
	static public QName _JOBID_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "job-id");
	
	@MInject(injectionFactory = QueueManagerInjectionFactory.class)
	private QueueManager _queueMgr;
	
	public QueueServiceImpl() throws RemoteException
	{
		/* Indicate the port type name to the base class */
		super("QueuePortType");
		
		/* Now we have to add our own port types to the list of port types
		 * implemented by this service.
		 */
		addImplementedPortType(QueueConstants.QUEUE_PORT_TYPE);
	}
	
	@Override
	protected void postCreate(ResourceKey key, EndpointReferenceType newEPR,
		ConstructionParameters cParams, HashMap<QName, Object> constructionParameters,
		Collection<MessageElement> resolverCreationParameters)
			throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(key, newEPR, cParams, constructionParameters,
			resolverCreationParameters);
		
		IQueueResource resource = (IQueueResource)key.dereference();
		resource.setEPR(newEPR);
	}

	public PortType getFinalWSResourceInterface()
	{
		return QueueConstants.QUEUE_PORT_TYPE;
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object completeJobs(String[] completeRequest) throws RemoteException
	{
		try
		{
			_queueMgr.completeJobs(completeRequest);
			return null;
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to complete jobs in queue.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object rescheduleJobs(String[] jobs) throws RemoteException
	{
		try
		{
			_queueMgr.rescheduleJobs(jobs);
			return null;
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to reschedule jobs in queue.", sqe);
		}
	}
	
	@Override
	@RWXMapping(RWXCategory.WRITE)
	public Object configureResource(ConfigureRequestType configureRequest)
			throws RemoteException
	{
		try
		{
			_queueMgr.configureBES(configureRequest.getQueueResource(), 
				configureRequest.getNumSlots().intValue());
			return null;
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to add bes container.", sqe);
		}
	}

	private JobInformationType[] getStatus(String[] getStatusRequest)
			throws RemoteException
	{
		Collection<JobInformationType> jobs;
		
		try
		{
			jobs = _queueMgr.getJobStatus(getStatusRequest);
			return jobs.toArray(new JobInformationType[0]);
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to list jobs in queue.", sqe);
		}
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	public IterateStatusResponseType iterateStatus(String[] iterateStatusRequest)
			throws RemoteException
	{
		Collection<MessageElement> col = new LinkedList<MessageElement>();
		
		for (JobInformationType jit : getStatus(iterateStatusRequest))
			col.add(AnyHelper.toAny(jit));
		
		try
		{
			return new IterateStatusResponseType(super.createWSIterator(
				col.iterator(), 100));
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to create iterator.", sqe);
		}
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	public JobErrorPacket[] queryErrorInformation(QueryErrorRequest arg0)
			throws RemoteException
	{
		try
		{
			return _queueMgr.queryErrorInformation(arg0.getJobTicket());
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to complete jobs in queue.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetJobLogResponse getJobLog(GetJobLogRequest arg0)
			throws RemoteException
	{
		try
		{
			return _queueMgr.getJobLog(arg0.getJobTicket());
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to get job log endpoint.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object killJobs(String[] killRequest) throws RemoteException
	{
		try
		{
			_queueMgr.killJobs(killRequest);
			return null;
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to list jobs in queue.", sqe);
		}
	}

	private ReducedJobInformationType[] listJobs(Object listRequest)
			throws RemoteException
	{
		Collection<ReducedJobInformationType> jobs;
		
		try
		{
			jobs = _queueMgr.listJobs(null);
			return jobs.toArray(new ReducedJobInformationType[0]);
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to list jobs in queue.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public IterateListResponseType iterateListJobs(Object iterateListRequest)
			throws RemoteException
	{
		Collection<MessageElement> col = new LinkedList<MessageElement>();
		
		for (ReducedJobInformationType rjit : listJobs(iterateListRequest))
		{
			col.add(AnyHelper.toAny(rjit));
		}
		
		try
		{
			return new IterateListResponseType(
				super.createWSIterator(col.iterator(), 100));
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to create iterator.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public IterateHistoryEventsResponseType iterateHistoryEvents(
		IterateHistoryEventsRequestType arg0) throws RemoteException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		
		if (arg0 != null && arg0.getResourceHint() != null)
		{
			try
			{
				_queueMgr.getJobStatus(new String[] { arg0.getResourceHint() });
			}
			catch (SQLException sqe)
			{
				throw new RemoteException(
					"Unable to check that job exists.", sqe);
			}
			
			arg0.setResourceHint(new QueueDatabase(
				rKey.getResourceKey()).historyKey(
					arg0.getResourceHint()));
		}
		
		return super.iterateHistoryEvents(arg0);
	}
	
	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public SubmitJobResponseType submitJob(SubmitJobRequestType submitJobRequest)
			throws RemoteException
	{
		String ticket;
		SweepListenerImpl listener;
		
		try
		{
			JobDefinition jobDefinition = JSDLUtils.convert(
				submitJobRequest.getJobDefinition());
			if (jobDefinition.parameterSweeps().size() > 0)
			{
				SweepToken token;
				token = SweepUtility.performSweep(jobDefinition, 
					listener = new SweepListenerImpl(
						_queueMgr, submitJobRequest.getPriority()));
				token.join();
				ticket = listener.firstTicket();
			} else
			{
				ticket = _queueMgr.submitJob(submitJobRequest.getPriority(), 
					submitJobRequest.getJobDefinition());
			}
			
			return new SubmitJobResponseType(ticket);
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to submit job to queue.", ioe);
		}
		catch (InterruptedException ie)
		{
			throw new RemoteException(
				"Unable to wait for first ticket to get generated.", ie);
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to submit job to queue.", sqe);
		}
		catch (JAXBException e)
		{
			throw new RemoteException("Unable to submit job to queue.", e);
		}
		catch (SweepException e)
		{
			throw new RemoteException("Unable to submit job to queue.", e);
		}
	}

	/*
	 * This method is called automatically when the Web server first comes up.
	 * We use it to restart the queue from where it left off.
	 */
	@Override
	public boolean startup()
	{
		boolean serviceCreated = super.startup();
		
		try
		{
			/* In order to make out calls, we have to have a working context
			 * so we go ahead and create an empty one.
			 */
			WorkingContext.setCurrentWorkingContext(new WorkingContext());
			
			/* Now we get the database connection pool configured 
			 * with this service */
			DatabaseConnectionPool connectionPool = (
				(QueueDBResourceFactory)ResourceManager.getServiceResource(_serviceName
					).getProvider().getFactory()).getConnectionPool();
			
			_logger.debug("Restarting all BES Managers.");
			QueueManager.startAllManagers(connectionPool);
		}
		catch (Exception e)
		{
			_logger.error("Unable to start resource info managers.", e);
		}
		finally
		{
			WorkingContext.setCurrentWorkingContext(null);
		}
		
		return serviceCreated;
	}
	
	@Override
	protected void preDestroy() throws RemoteException, ResourceException
	{
		super.preDestroy();
		
		try
		{
			_queueMgr.close();
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to pre-destroy queue.", ioe);
		}
	}
	
	@Override
	protected void registerNotificationHandlers(
			NotificationMultiplexer multiplexer)
	{
		super.registerNotificationHandlers(multiplexer);
		
		multiplexer.registerNotificationHandler(
			BESActivityTopics.ACTIVITY_STATE_CHANGED_TO_FINAL_TOPIC.asConcreteQueryExpression(),
			new LegacyBESActivityStateChangeFinalNotificationHandler());
	}

	private class LegacyBESActivityStateChangeFinalNotificationHandler
		extends AbstractNotificationHandler<BESActivityStateChangedContents>
	{
		private LegacyBESActivityStateChangeFinalNotificationHandler()
		{
			super(BESActivityStateChangedContents.class);
		}

		@Override
		public void handleNotification(TopicPath topic,
				EndpointReferenceType producerReference,
				EndpointReferenceType subscriptionReference,
				BESActivityStateChangedContents contents) throws Exception
		{
			JobCompletedAdditionUserData userData = 
				contents.additionalUserData(
					JobCompletedAdditionUserData.class);
			
			if (userData == null)
				throw new RemoteException(
					"Missing required user data for notification");
			
			long jobid = userData.jobID();
			ActivityState state = contents.activityState();
			if (state.isFinalState())
				_queueMgr.checkJobStatus(jobid);
		}
	}
	
	public void submitJobStream(InputStream in)
		throws IOException
	{
		if (!in.markSupported())
			throw new IOException(
				"Can only submit jobs from streams that support marking.");
		
		in.mark(Integer.MAX_VALUE);
		
		if (!submitJobTrySingle(in))
		{
			in.reset();
			submitJobTryMulti(in);
		}
	}
	
	private boolean submitJobTryMulti(InputStream in)
		throws IOException
	{
		JobDefinition_Type []jobDefs = 
			((JobMultiDefinition_Type)ObjectDeserializer.deserialize(
				new InputSource(in), JobMultiDefinition_Type.class)).getJobDefinition();
		if (jobDefs == null)
			return false;
		
		for (JobDefinition_Type jobDef : jobDefs)
			submitJob(new SubmitJobRequestType(jobDef, (byte)0x0));
		
		return true;
	}
	
	private boolean submitJobTrySingle(InputStream in)
		throws IOException
	{
		JobDefinition_Type jobDef = 
			(JobDefinition_Type)ObjectDeserializer.deserialize(
				new InputSource(in), JobDefinition_Type.class);
		if (jobDef == null || jobDef.getJobDescription() == null)
			return false;
		
		submitJob(new SubmitJobRequestType(jobDef, (byte)0x0));
		return true;
	}
	
	private class SweepListenerImpl implements SweepListener
	{
		private WorkingContext _workingContext;
		private ICallingContext _callingContext;
		private QueueManager _queueManager;
		private Collection<String> _tickets;
		private short _prioroity;
		private int _count = 0;
		
		private String firstTicket() throws InterruptedException
		{
			synchronized(_tickets)
			{
				while (_tickets.isEmpty())
					_tickets.wait();
				
				return _tickets.iterator().next();
			}
		}
		
		private SweepListenerImpl(QueueManager queueManager,
			short priority) throws FileNotFoundException, IOException
		{
			_queueManager = queueManager;
			_tickets = new LinkedList<String>();
			_prioroity = priority;
			_callingContext = ContextManager.getCurrentContext();
			_workingContext = 
				(WorkingContext)WorkingContext.getCurrentWorkingContext().clone();
		}
		
		@Override
		public void emitSweepInstance(JobDefinition jobDefinition) 
			throws SweepException
		{
			Closeable token = null;
			
			try
			{
				WorkingContext.setCurrentWorkingContext(_workingContext);
				token = ContextManager.temporarilyAssumeContext(_callingContext);
				synchronized(_tickets)
				{
					_tickets.add(_queueManager.submitJob(_prioroity,
						JSDLUtils.convert(jobDefinition)));
					_tickets.notifyAll();
				}
				_logger.debug(String.format(
					"Submitted job %d from a parameter sweep.", ++_count));
			}
			catch (JAXBException je)
			{
				throw new SweepException(
					"Unable to convert JAXB type to Axis type.", je);
			}
			catch (ResourceException e)
			{
				throw new SweepException("Unable to submit job.", e);
			}
			catch (SQLException e)
			{
				throw new SweepException("Unable to submit job.", e);
			}
			catch (IOException e)
			{
				throw new SweepException("Unable to submit job.", e);
			}
			finally
			{
				StreamUtils.close(token);
				WorkingContext.setCurrentWorkingContext(null);
			}
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public Object forceUpdate(String[] arg0) throws RemoteException
	{
		for (String arg : arg0)
		{
			try
			{
				_queueMgr.forceBESUpdate(arg);
			} catch (SQLException e)
			{
				_logger.error(String.format(
					"Unable to force update for resource %s.", arg), e);
			}
		}
		
		return null;
	}
}