package edu.virginia.vcgr.genii.container.queue;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;
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
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.queue.resource.IQueueResource;
import edu.virginia.vcgr.genii.container.queue.resource.QueueDBResourceFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.queue.ConfigureRequestType;
import edu.virginia.vcgr.genii.queue.JobInformationType;
import edu.virginia.vcgr.genii.queue.QueuePortType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;
import edu.virginia.vcgr.genii.queue.SubmitJobRequestType;
import edu.virginia.vcgr.genii.queue.SubmitJobResponseType;

public class QueueServiceImpl extends GenesisIIBase implements QueuePortType
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(QueueServiceImpl.class);
	
	public QueueServiceImpl() throws RemoteException
	{
		super("QueuePortType");
		
		addImplementedPortType(WellKnownPortTypes.QUEUE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.RNS_SERVICE_PORT_TYPE);
	}
	
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
		HashMap<QName, Object> constructionParameters) 
		throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, newEPR, constructionParameters);
		
		ResourceInfoManager.createManager(rKey.getKey().toString());
		JobManager.createManager(rKey.getKey().toString());
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public SubmitJobResponseType submitJob(SubmitJobRequestType submitJobRequest)
			throws RemoteException
	{
		try
		{
			JobDefinition_Type jsdl = submitJobRequest.getJobDefinition();
			int priority = (int)submitJobRequest.getPriority();
			String jobTicket = (new GUID()).toString();
			ICallingContext callingContext = ContextManager.getCurrentContext();
			Collection<Identity> identities = QueueSecurity.getCallerIdentities();
			
			IQueueResource resource = 
				(IQueueResource)ResourceManager.getCurrentResource().dereference();
			resource.submitJob(callingContext, jobTicket, jsdl, priority, identities);
			resource.commit();
			JobManager.getManager(
				resource.getKey().toString()).jobSchedulingOpportunity();
			return new SubmitJobResponseType(jobTicket);
		}
		catch (ConfigurationException ce)
		{
			throw new RemoteException("Unable to submit job.", ce);
		}
		catch (IOException ioe)
		{
			throw new RemoteException("Unable to submit job.", ioe);
		}
	}
	
	@RWXMapping(RWXCategory.READ)
	public JobInformationType[] getStatus(String[] getStatusRequest)
		throws RemoteException
	{
		IQueueResource resource = 
			(IQueueResource)ResourceManager.getCurrentResource().dereference();
		
		return resource.getStatus(getStatusRequest);
	}

	@RWXMapping(RWXCategory.READ)
	public ReducedJobInformationType[] listJobs(Object listRequest)
			throws RemoteException
	{
		IQueueResource resource = 
			(IQueueResource)ResourceManager.getCurrentResource().dereference();
		
		return resource.listJobs();
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public Object killJobs(String[] killRequest) throws RemoteException
	{
		IQueueResource resource = 
			(IQueueResource)ResourceManager.getCurrentResource().dereference();
		
		resource.killJobs(killRequest);
		return null;
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public Object completeJobs(String[] completeRequest) throws RemoteException
	{
		IQueueResource resource =
			(IQueueResource)ResourceManager.getCurrentResource().dereference();
		
		if (completeRequest == null)
			resource.completeAll();
		else
			resource.complete(completeRequest);
		
		return null;
	}

	@RWXMapping(RWXCategory.WRITE)
	public Object configureResource(ConfigureRequestType configureRequest)
			throws RemoteException
	{
		String resourceName = configureRequest.getQueueResource();
		int numSlots = configureRequest.getNumSlots().intValue();
		
		IQueueResource resource = 
			(IQueueResource)ResourceManager.getCurrentResource().dereference();
		resource.configureResource(resourceName, numSlots);
		
		return null;
	}

	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		String resourceName = addRequest.getEntry_name();
		EndpointReferenceType resourceEndpoint = addRequest.getEntry_reference();
		
		IQueueResource resource = 
			(IQueueResource)ResourceManager.getCurrentResource().dereference();
		resource.addResource(resourceName, resourceEndpoint);
		
		return new AddResponse(resourceEndpoint);
	}

	@RWXMapping(RWXCategory.WRITE)
	public CreateFileResponse createFile(CreateFile createFileRequest) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("createFile operation not supported in queue.");
	}

	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List listRequest) 
		throws RemoteException, ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		String regex = listRequest.getEntry_name_regexp();
		Pattern pattern = Pattern.compile(regex);
		Collection<EntryType> entries;
		
		IQueueResource resource = 
			(IQueueResource)ResourceManager.getCurrentResource().dereference();
		entries = resource.listResources(pattern);
		
		return new ListResponse(entries.toArray(new EntryType[0]));
	}

	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move moveRequest) 
		throws RemoteException, ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("Move operation not supported in queue.");
	}

	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query queryRequest) 
		throws RemoteException, ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("Query operation not supported in queue.");
	}

	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove removeRequest) 
		throws RemoteException, ResourceUnknownFaultType, 
			RNSDirectoryNotEmptyFaultType, RNSFaultType
	{
		String regex = removeRequest.getEntry_name();
		
		IQueueResource resource = 
			(IQueueResource)ResourceManager.getCurrentResource().dereference();
		return resource.remove(Pattern.compile(regex)).toArray(new String[0]);
	}
	
	public boolean startup()
	{
		boolean serviceCreated = super.startup();
		
		try
		{
			WorkingContext.setCurrentWorkingContext(new WorkingContext());
			DatabaseConnectionPool connectionPool =(
				(QueueDBResourceFactory)ResourceManager.getServiceResource(_serviceName
					).getProvider().getFactory()).getConnectionPool();
			ResourceInfoManager.startAllManagers(connectionPool);
			JobManager.startAllManagers(connectionPool);
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