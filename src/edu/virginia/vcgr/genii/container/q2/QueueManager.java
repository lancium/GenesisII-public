package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMResult;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;
import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Document;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.ResourceManagerType;
import edu.virginia.vcgr.genii.client.queue.CurrentResourceInformation;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.utils.Duration;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.infomgr.InMemoryPersister;
import edu.virginia.vcgr.genii.container.cservices.infomgr.InformationContainerService;
import edu.virginia.vcgr.genii.container.cservices.infomgr.InformationPortal;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.q2.besinfo.BESInformation;
import edu.virginia.vcgr.genii.container.q2.besinfo.BESInformationResolver;
import edu.virginia.vcgr.genii.container.q2.summary.HostDescription;
import edu.virginia.vcgr.genii.container.q2.summary.ResourceSummary;
import edu.virginia.vcgr.genii.container.q2.summary.SlotSummary;
import edu.virginia.vcgr.genii.container.rns.LegacyEntryType;
import edu.virginia.vcgr.genii.queue.GetJobLogResponse;
import edu.virginia.vcgr.genii.queue.JobErrorPacket;
import edu.virginia.vcgr.genii.queue.JobInformationType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

/**
 * This class is called directly from the queue service.  Mostly, it acts
 * as a conduit between the service impl, and the three managers underneath
 * (the BESManager, JobManager, and Scheduler).  The one wrinkle is that it
 * also implements a factory pattern to automatically load a QueueManager for
 * every queue registered in the system.  It also maintains threaded workers
 * such that there are only the minimum necessary per queue. 
 * 
 * @author mmm2a
 */
public class QueueManager implements Closeable
{
	/**
	 * To optimize hash table size, we assume a small
	 * number of managers to allocate space for.  If we
	 * have more, the table will grow appropriately, but
	 * there's no reason to waste memory.
	 */
	static private final int _DEFAULT_MANAGER_COUNT = 4;
	
	/**
	 * The maximum number of simultaneous outcalls the queue
	 * will allow.  This is reflected as threads in a
	 * thread pool.  This number of threads is shared by ALL
	 * queue instances on a server.
	 */
	static private final int _MAX_SIMULTANEOUS_OUTCALLS = 8;
	
	static private Log _logger = LogFactory.getLog(QueueManager.class);
	
	/**
	 * The database connection pool from whence to acquire
	 * temporary connections to the database.
	 */
	static private DatabaseConnectionPool _connectionPool = null;
	
	/**
	 * A map of queue key to queue manager for all instances running
	 * on this container.
	 */
	static private HashMap<String, QueueManager> _queueManager =
		new HashMap<String, QueueManager>(_DEFAULT_MANAGER_COUNT);
	
	static private InformationPortal<BESInformation> _informationPortal = null;
	
	synchronized static InformationPortal<BESInformation> 
		informationPortal()
	{
		if (_informationPortal == null)
		{
			InformationContainerService service = 
				ContainerServices.findService(
					InformationContainerService.class);
			_informationPortal = service.createNewPortal(
				new InMemoryPersister<BESInformation>(),
				new BESInformationResolver(_connectionPool),
				new Duration(30, TimeUnit.SECONDS),
				new Duration(10, TimeUnit.MINUTES));
		}
		
		return _informationPortal;
	}
	
	synchronized static public Collection<String> listAllQueues()
	{
		return new Vector<String>(_queueManager.keySet());
	}
	
	/**
	 * Create and start (active threads) all queue managers registered
	 * in the database right now.
	 * 
	 * @param connectionPool The connection pool to store as our 
	 * _connectionPool
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	static public void startAllManagers(DatabaseConnectionPool connectionPool)
		throws SQLException, ResourceException
	{
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		synchronized(QueueManager.class)
		{
			if (_connectionPool != null)
				throw new IllegalArgumentException("Queue managers already started.");
			
			_connectionPool = connectionPool;
		}
		
		try
		{
			/* Acquire a new connection to access the database with. */
			connection = _connectionPool.acquire(true);
			
			/* We look through the resources table to find all queueid's
			 * indicated.  We could equally have used the jobs table, but
			 * there will generally be less entries in the resources
			 * table.  This means that a queue which has no resources
			 * won't get started by default, but then again, it has no
			 * need to get started.  It also means that if someone creates
			 * a brand new system (bootstraps) but doesn't clean up the DB
			 * from the old one, we may have unnecessary queues running, but
			 * that is a very unlikely case.
			 */
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT queueid FROM q2resources");
			
			while (rs.next())
			{
				/* Simplly accessing the manager in question causes it to
				 * get created and started if it isn't already.
				 */
				getManager(rs.getString(1));
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	/**
	 * Get the queue manager for a given queue key (create it if it 
	 * doesn't already exist).
	 * 
	 * @param queueid THe queue key to acquire a manager for.
	 * 
	 * @return The queue manager with the given key.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	static public QueueManager getManager(String queueid) 
		throws SQLException, ResourceException
	{
		QueueManager mgr;
		
		synchronized(_queueManager)
		{
			mgr = _queueManager.get(queueid);
			if (mgr == null)
				_queueManager.put(queueid, mgr = new QueueManager(queueid));
		}
		
		return mgr;
	}
	
	/**
	 * Destroy an active queue manager.  Destroy is a misnomer here -- actually
	 * it simply stops the threads and shuts it down.  If there is still data
	 * in the database, the manager may get loaded again the next time the
	 * container restarts.  This is mostly for shutting down a queue.
	 * 
	 * @param queueid
	 */
	static public void destroyManager(String queueid)
	{
		QueueManager mgr = null;
		
		synchronized(_queueManager)
		{
			mgr = _queueManager.remove(queueid);
		}
		
		if (mgr != null)
			StreamUtils.close(mgr);
	}
	
	volatile private boolean _closed = false;
	private String _queueid;
	
	/**
	 * The outcall thread pool manager.
	 */
	private ThreadPool _outcallThreadPool = null;
	
	private BESManager _besManager;
	private JobManager _jobManager;
	private Scheduler _scheduler;
	private QueueDatabase _database;
	private SchedulingEvent _schedulingEvent;
	
	private CurrentResourceInformation getCurrentResourceInformation(
		String entryName) throws ResourceException
	{
		BESData data = _besManager.getBESData(entryName);
		BESInformation info = _besManager.getBESInformation(data.getID());
		BESUpdateInformation updateInfo = _besManager.getUpdateInformation(
			data.getID());
		
		boolean isAccepting = (info == null) ? false : info.isAcceptingNewActivities();
		ProcessorArchitecture arch = (info == null) ? ProcessorArchitecture.other :
			ProcessorArchitecture.valueOf(info.getProcessorArchitecture().toString());
		OperatingSystemNames osName = (info == null) ? OperatingSystemNames.Unknown :
			OperatingSystemNames.valueOf(info.getOperatingSystemType().toString());
		String osVersion = (info == null) ? null : info.getOperatingSystemVersion();
		Double physicalMemory = (info == null) ? null : info.getPhysicalMemory();
		ResourceManagerType mgrType = (info == null) ? ResourceManagerType.Unknown :
			ResourceManagerType.fromURI(info.resourceManagerType());
		
		boolean isAvailable = (updateInfo == null) ?
			false : updateInfo.isAvailable();
		Date lastUpdated = (updateInfo == null) ? null :
			updateInfo.lastUpdated();
		Date nextUpdate = (updateInfo == null) ? null :
			updateInfo.nextUpdate();
		
		HashMap<Long, SlotSummary> slots = new HashMap<Long, SlotSummary>();
		slots.put(new Long(data.getID()), new SlotSummary(data.getTotalSlots(), 0));
		_jobManager.recordUsedSlots(slots);
		return new CurrentResourceInformation(data.getTotalSlots(),
			(int)(slots.get(data.getID()).slotsUsed()),
			isAccepting, arch, osName, osVersion, physicalMemory,
			mgrType, isAvailable, lastUpdated, nextUpdate);
	}
	
	private Collection<LegacyEntryType> addInCurrentResourceInformation(
		Collection<LegacyEntryType> entries) throws JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(
			CurrentResourceInformation.class);
		Marshaller m = context.createMarshaller();
		
		for (LegacyEntryType entry : entries)
		{
			try
			{
				MessageElement []any = entry.get_any();
				if (any != null && any.length > 0)
				{
					MessageElement []tmp = new MessageElement[any.length + 1];
					System.arraycopy(any, 0, tmp, 0, any.length);
					any = tmp;
				} else
					any = new MessageElement[1];
				
				CurrentResourceInformation cri = getCurrentResourceInformation(
					entry.getEntry_name());
				DOMResult result = new DOMResult();
				m.marshal(cri, result);
				any[any.length - 1] = new MessageElement(
					((Document)result.getNode()).getDocumentElement());
				entry.set_any(any);
			}
			catch (Throwable cause)
			{
				_logger.warn(String.format(
					"Unable to marshall current resource information for %s.", 
					entry.getEntry_name()), cause);
			}
		}
		
		return entries;
	}
	
	/**
	 * Private constructor used to create a new active queue manager.  This
	 * instance will start up all of the sub-managers like the BESManager, 
	 * JobManager, and Scheduler.
	 * 
	 * @param queueid The ID for the queue.
	 * 
	 * @throws SQLException
	 * @throws ResourceException
	 */
	private QueueManager(String queueid) 
		throws SQLException, ResourceException
	{
		Connection connection = null;
		_outcallThreadPool = new ThreadPool(_MAX_SIMULTANEOUS_OUTCALLS);
		_queueid = queueid;
		_database = new QueueDatabase(_queueid);
		_schedulingEvent = new SchedulingEvent();
		
		try
		{
			connection = _connectionPool.acquire(true);
			_besManager = new BESManager(
				_database, _schedulingEvent, 
				connection, informationPortal(), _connectionPool);
			_jobManager = new JobManager(_outcallThreadPool,
				_database, _schedulingEvent, _besManager, connection, _connectionPool);
			_scheduler = new Scheduler(_queueid, _schedulingEvent, _connectionPool,
				_jobManager, _besManager);
		}
		catch (GenesisIISecurityException gse)
		{
			throw new ResourceException("UInable to create BES Manager.", gse);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	synchronized public void close() throws IOException
	{
		if (_closed)
			return;
		_closed = true;
		
		/* Stop the running threads */
		StreamUtils.close(_scheduler);
		StreamUtils.close(_besManager);
		StreamUtils.close(_jobManager);
		StreamUtils.close(_outcallThreadPool);
	}
		
	/************************************************************************/
	/* The remainder of the methods in this class correspond exactly to     */
	/* methods in the various managers (BESManager and JobManager) with the */
	/* exception that they all take a database connection and these don't.  */
	/* These methods acquire the connection and release it and then call    */
	/* through to the back-end methods.                                      */
	/************************************************************************/
	
	public void addNewBES(String name, EndpointReferenceType epr)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(false);
			_besManager.addNewBES(connection, name, epr);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public void forceBESUpdate(String name)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(false);
			_besManager.forceUpdate(connection, name);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public void configureBES(String name, 
		int newSlots) throws SQLException, ResourceException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(false);
			_besManager.configureBES(connection, name, newSlots);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public int getBESConfiguration(String name) throws ResourceException
	{
		return _besManager.getConfiguration(name);
	}
	
	public Scheduler getScheduler()
	{
		return _scheduler;
	}
	
	public JobDefinition_Type getJSDL(String jobTicket)
		throws ResourceException, SQLException
	{
		return _jobManager.getJSDL(jobTicket);
	}
	
	public void printLog(String jobTicket, PrintStream out) throws IOException
	{
		_jobManager.printLog(jobTicket, out);
	}
	
	public Collection<LegacyEntryType> listBESs(String entryName)
		throws SQLException, ResourceException
	{
		Connection connection = null;
		Collection<LegacyEntryType> ret = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			ret = _besManager.listBESs(
				connection, entryName);
			addInCurrentResourceInformation(ret);
		}
		catch (JAXBException e)
		{
			_logger.warn(
				"Error trying to add in current resource information.", e);
		}
		finally
		{
			_connectionPool.release(connection);
		}
		
		return ret;
	}
	
	public Collection<String> removeBESs(String entryName) throws SQLException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(false);
			return _besManager.removeBESs(connection, entryName);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public String submitJob(short priority, JobDefinition_Type jsdl)
		throws SQLException, ResourceException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(false);
			return _jobManager.submitJob(connection, jsdl, priority);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public Collection<ReducedJobInformationType> listJobs(String ticket)
		throws SQLException, ResourceException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			return _jobManager.listJobs(connection, ticket);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public EndpointReferenceType getActivityEPR(String ticket)
		throws ResourceException, SQLException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			return _jobManager.getActivityEPR(connection, ticket);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public void checkJobStatus(long jobID) 
		throws SQLException
	{
		_jobManager.checkJobStatus(jobID);
	}
	
	public Collection<JobInformationType> getJobStatus(String []jobs)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			return _jobManager.getJobStatus(connection, jobs);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public JobErrorPacket[] queryErrorInformation(String job)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			return _jobManager.queryErrorInformation(connection, job);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public GetJobLogResponse getJobLog(String job) throws ResourceException, SQLException
	{
		return new GetJobLogResponse(
			_jobManager.getLogEPR(job));
	}
	
	public void completeJobs(String []jobs)
		throws SQLException, ResourceException, 
			GenesisIISecurityException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(false);
			_jobManager.completeJobs(connection, jobs);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public void rescheduleJobs(String []jobs)
		throws SQLException, ResourceException, 
			GenesisIISecurityException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(false);
			_jobManager.rescheduleJobs(connection, jobs);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public void killJobs(String []jobs)
		throws SQLException, ResourceException, GenesisIISecurityException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(false);
			_jobManager.killJobs(connection, jobs);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public Map<String, Long> summarizeJobs()
	{
		return _jobManager.summarizeToMap();
	}
	
	public void summarize(PrintStream out) throws IOException, SQLException
	{
		_besManager.summarize(out);
		Map<String, Long> jobMap = _jobManager.summarizeToMap();
		
		out.println("Queue Job Summary\n-----------------------------\n");
		for (String category : jobMap.keySet())
		{
			out.format("\t%s:  %d\n", category, jobMap.get(category));
		}
	}
	
	public long totalSlots()
	{
		long total = 0L;
		
		synchronized(_besManager)
		{
			/* Get all available resources */
			Collection<BESData> allResources = _besManager.getAllBESs();

			/* If we didn't get any resources back, then there's no reason
			 * to continue.
			 */
			if (allResources.size() == 0)
				return total;
			
			/* Now we go through the list and get rid of all resources that
			 * had no slots allocated.
			 */
			for (BESData data : allResources)
				total += data.getTotalSlots();
		}
		
		return total;
	}
	
	public long totalFinishedAllTime()
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			return _database.getTotalFinished(connection);
		}
		catch (SQLException sqe)
		{
			_logger.warn("Unable to acqurie total job finished count.", sqe);
			return 0L;
		}
		finally
		{
			if (connection != null)
				_connectionPool.release(connection);
		}
	}
	
	public ResourceSummary summarize()
	{
		ResourceSummary summary = new ResourceSummary();
		Map<Long, SlotSummary> hostSlotSummary = 
			new HashMap<Long, SlotSummary>();
		
		synchronized(_besManager)
		{
			/* Get all available resources */
			Collection<BESData> allResources = _besManager.getAllBESs();

			/* If we didn't get any resources back, then there's no reason
			 * to continue.
			 */
			if (allResources.size() == 0)
				return summary;
			
			/* Now we go through the list and get rid of all resources that
			 * had no slots allocated.
			 */
			for (BESData data : allResources)
				hostSlotSummary.put(data.getID(), 
					new SlotSummary(data.getTotalSlots(), 0L));
		}
		
		synchronized(_jobManager)
		{
			_jobManager.recordUsedSlots(hostSlotSummary);
		}
		
		synchronized(_besManager)
		{
			for (Long besID : hostSlotSummary.keySet())
			{
				BESInformation info = _besManager.getBESInformation(besID);
				if (info != null)
				{
					summary.add(new HostDescription(
						info.getProcessorArchitecture(), 
						info.getOperatingSystemType()),
						hostSlotSummary.get(besID));
				}
			}
		}
		
		return summary;
	}
	
	public BESManager getBESManager()
	{
		return _besManager;
	}
}
