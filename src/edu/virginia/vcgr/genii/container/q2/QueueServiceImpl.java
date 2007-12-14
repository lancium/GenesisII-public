package edu.virginia.vcgr.genii.container.q2;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.q2.resource.QueueDBResourceFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.queue.ConfigureRequestType;
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
	
	public QueueServiceImpl() throws RemoteException
	{
		/* Indicate the port type name to the base class */
		super("QueuePortType");
		
		/* Now we have to add our own port types to the list of port types
		 * implemented by this service.
		 */
		addImplementedPortType(WellKnownPortTypes.QUEUE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.RNS_SERVICE_PORT_TYPE);
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
		catch (ConfigurationException ce)
		{
			throw new RemoteException("Unable to add bes container.", ce);
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
	@RWXMapping(RWXCategory.WRITE)
	public CreateFileResponse createFile(CreateFile createFileRequest)
			throws RemoteException, RNSEntryExistsFaultType, RNSFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType
	{
		throw new RemoteException(
			"createFile operation not supported for Queues.");
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public JobInformationType[] getStatus(String[] getStatusRequest)
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
		Pattern pattern = Pattern.compile(listRequest.getEntry_name_regexp());
		Collection<EntryType> entries;
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			entries = mgr.listBESs(pattern);
			return new ListResponse(entries.toArray(new EntryType[0]));
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to add bes container.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public ReducedJobInformationType[] listJobs(Object listRequest)
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
		Pattern pattern = Pattern.compile(removeRequest.getEntry_name());
		ResourceKey rKey = ResourceManager.getCurrentResource();
		Collection<String> entries = new ArrayList<String>();
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			entries = mgr.removeBESs(pattern);
			
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
		catch (ConfigurationException ce)
		{
			throw new RemoteException("Unable to submit job to queue.", ce);
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
}