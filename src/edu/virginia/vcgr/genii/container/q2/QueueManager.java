package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.rns.EntryType;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.queue.JobInformationType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;

public class QueueManager implements Closeable
{
	static private final int _DEFAULT_MANAGER_COUNT = 4;
	static private final int _MAX_SIMULTANEOUS_OUTCALLS = 8;
	
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(QueueManager.class);
	
	static private DatabaseConnectionPool _connectionPool = null;
	static private ThreadPool _outcallThreadPool = null;
	
	static private HashMap<String, QueueManager> _queueManager =
		new HashMap<String, QueueManager>(_DEFAULT_MANAGER_COUNT);
	
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
			connection = _connectionPool.acquire();
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT queueid FROM q2resources");
			
			while (rs.next())
			{
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
	
	static public QueueManager getManager(String queueid) 
		throws SQLException, ResourceException
	{
		QueueManager mgr;
		
		synchronized(_queueManager)
		{
			mgr = _queueManager.get(queueid);
			if (mgr == null)
			{
				synchronized(QueueManager.class)
				{
					if (_outcallThreadPool == null)
						_outcallThreadPool = new ThreadPool(_MAX_SIMULTANEOUS_OUTCALLS);
				}
				
				_queueManager.put(queueid, mgr = new QueueManager(queueid));
			}
		}
		
		return mgr;
	}
	
	static public void destroyManager(String queueid)
	{
		boolean empty;
		QueueManager mgr = null;
		
		synchronized(_queueManager)
		{
			mgr = _queueManager.remove(queueid);
			empty = _queueManager.isEmpty();
		}
		
		if (mgr != null)
			StreamUtils.close(mgr);
		
		synchronized(QueueManager.class)
		{
			if (empty && _outcallThreadPool != null)
			{
				StreamUtils.close(_outcallThreadPool);
				_outcallThreadPool = null;
			}
		}
	}
	
	volatile private boolean _closed = false;
	private String _queueid;
	
	private BESManager _besManager;
	private JobManager _jobManager;
	private Scheduler _scheduler;
	private QueueDatabase _database;
	private SchedulingEvent _schedulingEvent;
	
	private QueueManager(String queueid) 
		throws SQLException, ResourceException
	{
		Connection connection = null;
		_queueid = queueid;
		_database = new QueueDatabase(_queueid);
		_schedulingEvent = new SchedulingEvent();
		
		try
		{
			connection = _connectionPool.acquire();
			_besManager = new BESManager(_outcallThreadPool,
				_database, _schedulingEvent, 
				connection, _connectionPool);
			_jobManager = new JobManager(_outcallThreadPool,
				_database, _schedulingEvent, connection, _connectionPool);
			_scheduler = new Scheduler(_schedulingEvent, _connectionPool,
				_jobManager, _besManager);
		}
		catch (GenesisIISecurityException gse)
		{
			throw new ResourceException("UInable to create BES Manager.", gse);
		}
		catch (ConfigurationException ce)
		{
			throw new ResourceException("Unable to create BES Manager.", ce);
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
		StreamUtils.close(_scheduler);
		StreamUtils.close(_besManager);
		StreamUtils.close(_jobManager);
	}
	
	public void addNewBES(String name, EndpointReferenceType epr)
		throws SQLException, ResourceException, ConfigurationException,
			GenesisIISecurityException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire();
			_besManager.addNewBES(connection, name, epr);
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
			connection = _connectionPool.acquire();
			_besManager.configureBES(connection, name, newSlots);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public Collection<EntryType> listBESs(Pattern pattern)
		throws SQLException, ResourceException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire();
			return _besManager.listBESs(connection, pattern);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public Collection<String> removeBESs(Pattern pattern) throws SQLException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire();
			return _besManager.removeBESs(connection, pattern);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public String submitJob(short priority, JobDefinition_Type jsdl)
		throws SQLException, ResourceException, ConfigurationException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire();
			return _jobManager.submitJob(connection, jsdl, priority);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public Collection<ReducedJobInformationType> listJobs()
		throws SQLException, ResourceException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire();
			return _jobManager.listJobs(connection);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public Collection<JobInformationType> getJobStatus(String []jobs)
		throws SQLException, ResourceException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire();
			return _jobManager.getJobStatus(connection, jobs);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public void completeJobs(String []jobs)
		throws SQLException, ResourceException, 
			GenesisIISecurityException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire();
			_jobManager.completeJobs(connection, jobs);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public void killJobs(String []jobs)
		throws SQLException, ResourceException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire();
			_jobManager.killJobs(connection, jobs);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
}