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
import org.ggf.rns.EntryType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

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
		throws SQLException
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
	
	static public QueueManager getManager(String queueid) throws SQLException
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
	private QueueDatabase _database;
	private SchedulingEvent _schedulingEvent;
	
	private QueueManager(String queueid) throws SQLException
	{
		Connection connection = null;
		_queueid = queueid;
		_database = new QueueDatabase(_queueid);
		_schedulingEvent = new SchedulingEvent();
		
		try
		{
			connection = _connectionPool.acquire();
			_besManager = new BESManager(_database, _schedulingEvent, connection);
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
		_besManager.close();
	}
	
	public void addNewBES(String name, EndpointReferenceType epr)
		throws SQLException, ResourceException
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
}