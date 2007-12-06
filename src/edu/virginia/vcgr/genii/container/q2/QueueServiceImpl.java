package edu.virginia.vcgr.genii.container.q2;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
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
import org.ws.addressing.EndpointReferenceType;

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
			mgr.addBESContainer(addRequest.getEntry_name(), addRequest.getEntry_reference());
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public Object configureResource(ConfigureRequestType configureRequest)
			throws RemoteException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public CreateFileResponse createFile(CreateFile createFileRequest)
			throws RemoteException, RNSEntryExistsFaultType, RNSFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public JobInformationType[] getStatus(String[] getStatusRequest)
			throws RemoteException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public Object killJobs(String[] killRequest) throws RemoteException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List listRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		Pattern pattern = Pattern.compile(listRequest.getEntry_name_regexp());
		HashMap<String, EndpointReferenceType> ret;
		Collection<EntryType> entries = new ArrayList<EntryType>();
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			ret = mgr.listEntries();
			for (String name : ret.keySet())
			{
				Matcher matcher = pattern.matcher(name);
				if (matcher.matches())
				{
					entries.add(new EntryType(name, null, ret.get(name)));
				}
			}
			
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move moveRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query queryRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove removeRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType,
			RNSDirectoryNotEmptyFaultType
	{
		Pattern pattern = Pattern.compile(removeRequest.getEntry_name());
		ResourceKey rKey = ResourceManager.getCurrentResource();
		HashMap<String, EndpointReferenceType> ret;
		Collection<String> entries = new ArrayList<String>();
		
		try
		{
			QueueManager mgr = QueueManager.getManager((String)rKey.getKey());
			ret = mgr.listEntries();
			for (String name : ret.keySet())
			{
				Matcher matcher = pattern.matcher(name);
				if (matcher.matches())
				{
					mgr.removeBESContainer(name);
					entries.add(name);
				}
			}
			
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean startup()
	{
		boolean serviceCreated = super.startup();
		
		try
		{
			WorkingContext.setCurrentWorkingContext(new WorkingContext());
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
