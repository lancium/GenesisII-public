package edu.virginia.vcgr.genii.container.q2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Token;
import org.apache.axis.types.UnsignedLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobMultiDefinition_Type;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.Move;
import org.ggf.rns.MoveResponse;
import org.ggf.rns.Query;
import org.ggf.rns.QueryResponse;
import org.ggf.rns.RNSDirectoryNotEmptyFaultType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ggf.rns.Remove;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.morgan.util.io.GuaranteedDirectory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.comm.ClientConstructionParameters;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.client.queue.QueueConstants;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.AnyHelper;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.common.notification.Notify;
import edu.virginia.vcgr.genii.common.notification.Subscribe;
import edu.virginia.vcgr.genii.common.notification.UserDataType;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.byteio.RByteIOResource;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.q2.resource.QueueDBResourceFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.queue.ConfigureRequestType;
import edu.virginia.vcgr.genii.queue.IterateListResponseType;
import edu.virginia.vcgr.genii.queue.IterateStatusResponseType;
import edu.virginia.vcgr.genii.queue.JobInformationType;
import edu.virginia.vcgr.genii.queue.QueuePortType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;
import edu.virginia.vcgr.genii.queue.SubmitJobRequestType;
import edu.virginia.vcgr.genii.queue.SubmitJobResponseType;

/**
 * This is the service class that the container redirects SOAP messages to.
 * 
 * @author mmm2a
 */
public class QueueServiceImpl extends GenesisIIBase implements QueuePortType
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(QueueServiceImpl.class);
	
	static private final long _DEFAULT_TIME_TO_LIVE = 1000L * 60 * 60;
	static private QName _FILENAME_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "create-file-filename");
	static private QName _FILEPATH_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "data-filepath");
	
	static private UserDataType createUserData(String filename, String filepath)
	{
		return new UserDataType(new MessageElement[] { 
			new MessageElement(
				_FILENAME_QNAME, filename),
			new MessageElement(
				_FILEPATH_QNAME, filepath)
		});
	}
	
	public QueueServiceImpl() throws RemoteException
	{
		/* Indicate the port type name to the base class */
		super("QueuePortType");
		
		/* Now we have to add our own port types to the list of port types
		 * implemented by this service.
		 */
		addImplementedPortType(QueueConstants.QUEUE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.RNS_SERVICE_PORT_TYPE);
	}
	
	public PortType getFinalWSResourceInterface()
	{
		return QueueConstants.QUEUE_PORT_TYPE;
	}
	
	@Override
	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) throws RemoteException,
			RNSEntryExistsFaultType, RNSFaultType, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		
		if (addRequest == null || addRequest.getEntry_reference() == null 
			|| addRequest.getEntry_name() == null)
			throw new RemoteException("Only allowed to add BES containers to Queues.");
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			mgr.addNewBES(addRequest.getEntry_name(), addRequest.getEntry_reference());
			return new AddResponse(addRequest.getEntry_reference());
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to add bes container.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object completeJobs(String[] completeRequest) throws RemoteException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			mgr.completeJobs(completeRequest);
			return null;
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to complete jobs in queue.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public Object configureResource(ConfigureRequestType configureRequest)
			throws RemoteException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			mgr.configureBES(configureRequest.getQueueResource(), 
				configureRequest.getNumSlots().intValue());
			return null;
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to add bes container.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponse createFile(CreateFile createFileRequest)
			throws RemoteException, RNSEntryExistsFaultType, RNSFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType
	{
		MessageElement []parameters = null;
		
		File filePath;
		
		try
		{
			File userDir = ConfigurationManager.getCurrentConfiguration().getUserDirectory();
			GuaranteedDirectory sbyteiodir = new GuaranteedDirectory(userDir, "sbyteio");
			filePath = File.createTempFile("sbyteio", ".dat", sbyteiodir);
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.getLocalizedMessage(), ioe);
		}
		
		Subscribe subscribeRequest = new Subscribe(new Token(
			WellknownTopics.SBYTEIO_INSTANCE_DYING),
			new UnsignedLong(_DEFAULT_TIME_TO_LIVE),
			(EndpointReferenceType)WorkingContext.getCurrentWorkingContext(
				).getProperty(WorkingContext.EPR_PROPERTY_NAME),
			createUserData(createFileRequest.getFilename(), 
				filePath.getAbsolutePath()));
			
		
		parameters = new MessageElement [] {
			new MessageElement(RByteIOResource.FILE_PATH_PROPERTY,
				filePath.getAbsolutePath()),
			new MessageElement(
				ByteIOConstants.SBYTEIO_SUBSCRIBE_CONSTRUCTION_PARAMETER,
				subscribeRequest),
			new MessageElement(
				ByteIOConstants.MUST_DESTROY_PROPERTY,
				Boolean.FALSE),
            new MessageElement(
            	ByteIOConstants.SBYTEIO_DESTROY_ON_CLOSE_FLAG,
            	Boolean.TRUE),
			ClientConstructionParameters.createTimeToLiveProperty(
				_DEFAULT_TIME_TO_LIVE)
		};
		
		StreamableByteIOPortType sbyteio = ClientUtils.createProxy(
			StreamableByteIOPortType.class, EPRUtils.makeEPR(
				Container.getServiceURL("StreamableByteIOPortType")));
		VcgrCreateResponse resp = sbyteio.vcgrCreate(new VcgrCreate(parameters));
		
		return new CreateFileResponse(resp.getEndpoint());
	}

	private JobInformationType[] getStatus(String[] getStatusRequest)
			throws RemoteException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		Collection<JobInformationType> jobs;
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			jobs = mgr.getJobStatus(getStatusRequest);
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
		{
			col.add(AnyHelper.toAny(jit));
		}
		
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
	public Object killJobs(String[] killRequest) throws RemoteException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			mgr.killJobs(killRequest);
			return null;
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to list jobs in queue.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List listRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		Collection<EntryType> entries;
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			entries = mgr.listBESs(listRequest.getEntryName());
			return new ListResponse(entries.toArray(new EntryType[0]));
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to add bes container.", sqe);
		}
	}

	private ReducedJobInformationType[] listJobs(Object listRequest)
			throws RemoteException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		Collection<ReducedJobInformationType> jobs;
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			jobs = mgr.listJobs();
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
	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move moveRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType
	{
		throw new RemoteException(
			"Move operation not supported for queues.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query queryRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType
	{
		throw new RemoteException(
			"Query operation not supported for queues.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove removeRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType,
			RNSDirectoryNotEmptyFaultType
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		Collection<String> entries = new ArrayList<String>();
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			entries = mgr.removeBESs(removeRequest.getEntryName());
			
			return entries.toArray(new String[0]);
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to add bes container.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public SubmitJobResponseType submitJob(SubmitJobRequestType submitJobRequest)
			throws RemoteException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			String ticket = mgr.submitJob(submitJobRequest.getPriority(), 
				submitJobRequest.getJobDefinition());
			
			return new SubmitJobResponseType(ticket);
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to submit job to queue.", sqe);
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
			DatabaseConnectionPool connectionPool =(
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
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			mgr.close();
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to pre-destroy queue.", sqe);
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to pre-destroy queue.", ioe);
		}
	}
	
	@RWXMapping(RWXCategory.OPEN)
	public void notify(Notify notify) throws RemoteException, ResourceUnknownFaultType
	{
		try
		{
			String topic = notify.getTopic().toString();
			String name = null;
			
			if (topic.equals(WellknownTopics.SBYTEIO_INSTANCE_DYING))
			{
				UserDataType userData = notify.getUserData();
				if (userData == null || (userData.get_any() == null) )
					throw new RemoteException(
						"Missing required user data for notification");
				MessageElement []data = userData.get_any();
				if (data.length != 2)
					throw new RemoteException(
						"Missing required user data for notification");
				String filepath = null;
				
				for (MessageElement elem : data)
				{
					QName elemName = elem.getQName();
					if (elemName.equals(_FILENAME_QNAME))
					{
						name = elem.getValue();
					} else if (elemName.equals(_FILEPATH_QNAME))
					{
						filepath = elem.getValue();
					} else
					{
						throw new RemoteException(
							"Unknown user data found in notification.");
					}
				}
				
				if (name == null)
					throw new ResourceException(
						"Couldn't locate name parameter in UserData for notification.");
				if (filepath == null)
					throw new ResourceException(
						"Couldn't locate filepath parameter in UserData " +
						"for notification.");
				
				if (!name.endsWith(".txt"))
					name += ".txt";
				
				submitJob(filepath);
			}
		}
		catch (Throwable t)
		{
			_logger.warn(t.getLocalizedMessage(), t);
		}
	}
	
	private void submitJob(String filepath)
		throws IOException
	{
		File file = new File(filepath);
		
		try
		{
			if (!submitJobTrySingle(file))
				submitJobTryMulti(file);
		}
		finally
		{
			file.delete();
		}
	}
	
	private boolean submitJobTryMulti(File file)
		throws IOException
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(file);
			JobDefinition_Type []jobDefs = 
				((JobMultiDefinition_Type)ObjectDeserializer.deserialize(
					new InputSource(fin), JobMultiDefinition_Type.class)).getJobDefinition();
			if (jobDefs == null)
				return false;
			
			for (JobDefinition_Type jobDef : jobDefs)
				submitJob(new SubmitJobRequestType(jobDef, (byte)0x0));
			
			return true;
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	private boolean submitJobTrySingle(File file)
		throws IOException
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(file);
			JobDefinition_Type jobDef = 
				(JobDefinition_Type)ObjectDeserializer.deserialize(
					new InputSource(fin), JobDefinition_Type.class);
			if (jobDef == null || jobDef.getJobDescription() == null)
				return false;
			
			submitJob(new SubmitJobRequestType(jobDef, (byte)0x0));
			return true;
		}
		catch (IOException ioe)
		{
			throw ioe;
		}
		catch (Throwable cause)
		{
			return false;
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
}