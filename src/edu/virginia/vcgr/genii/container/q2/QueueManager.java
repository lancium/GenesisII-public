package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.BESPortType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;

public class QueueManager implements Closeable
{
	static private final int _DEFAULT_MANAGER_COUNT = 4;
	static private final int _MAX_SIMULTANEOUS_OUTCALLS = 8;
	static private final long _BES_UPDATE_CYCLE = 1000L * 60 * 5;
	
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
	
	private Object _besWorkerLock = new Object();
	private HashMap<Long, BESInformation> _besByID = new HashMap<Long, BESInformation>();
	private HashMap<String, BESInformation> _besByName = new HashMap<String, BESInformation>();
	private HashMap<Long, BESInformation> _scheduleableBESs = new HashMap<Long, BESInformation>();
	private LinkedList<BESInformation> _newBESs = new LinkedList<BESInformation>();
	
	private Thread _besUpdaterThread;
	
	private Thread createWorkerThread(Runnable worker, String name)
	{
		Thread th = new Thread(worker);
		th.setDaemon(true);
		th.setName(name);
		
		return th;
	}
	
	private QueueManager(String queueid) throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		_queueid = queueid;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"SELECT resourceid, resourcename, totalslots " +
				"FROM q2resources WHERE queueid = ?");
			stmt.setString(1, queueid);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				long id = rs.getLong(1);
				String name = rs.getString(2);
				int slots = rs.getInt(3);
				
				BESInformation info = new BESInformation(id, name, slots);
				_besByID.put(new Long(id), info);
				_besByName.put(name, info);
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
		
		_besUpdaterThread = 
			createWorkerThread(new BESUpdaterWorker(), "BES Updater Worker");
		
		_besUpdaterThread.start();
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
	}
	
	public void addBESContainer(String name, EndpointReferenceType epr) throws SQLException, ResourceException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"INSERT INTO q2resources " +
					"(queueid, resourcename, resourceendpoint, totalslots) " +
				"VALUES (?, ?, ?, ?)");
			stmt.setString(1, _queueid);
			stmt.setString(2, name);
			stmt.setBlob(3, EPRUtils.toBlob(epr));
			stmt.setInt(4, 1);
			if (stmt.executeUpdate() != 1)
			{
				_logger.warn("Unknown error while trying to add a new bes container.");
				throw new ResourceException("Unable to add new BES resource.");
			}
			connection.commit();
			stmt.close();
			stmt = connection.prepareStatement("values IDENTITY_VAL_LOCAL()");
			rs = stmt.executeQuery();
			if (!rs.next())
			{
				// Couldn't findout the resource id we just added.
				throw new SQLException("Couldn't figure out generated id for last added resource.");
			}
			
			long id = rs.getLong(1);
			BESInformation info = new BESInformation(id, name, 1);
			
			synchronized(_newBESs)
			{
				_newBESs.add(info);
				_newBESs.notify();
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	public HashMap<String, EndpointReferenceType> listEntries()
		throws SQLException, ResourceException
	{
		HashMap<String, EndpointReferenceType> ret = new HashMap<String, EndpointReferenceType>();
		
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"SELECT resourcename, resourceendpoint FROM q2resources WHERE queueid = ?");
			stmt.setString(1, _queueid);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				ret.put(rs.getString(1), EPRUtils.fromBlob(rs.getBlob(2)));
			}
			
			return ret;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	public void removeBESContainer(String containerName)
		throws SQLException
	{
		long id;
		
		synchronized(_besWorkerLock)
		{
			BESInformation info = _besByName.remove(containerName);
			if (info != null)
			{
				synchronized(info)
				{
					id = info.getResourceID();
				}
				
				_besByID.remove(new Long(id));
				_scheduleableBESs.remove(new Long(id));
			}
		}
		
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement("DELETE FROM q2resources WHERE queueid = ? AND resourcename = ?");
			stmt.setString(1, _queueid);
			stmt.setString(2, containerName);
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to remove resource from queue.");
			connection.commit();
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	// Update BES information for ALL BES containers
	private void updateBESInformation() throws SQLException, ResourceException
	{
		_logger.debug("In updateBESInformation().");
		
		Collection<BESInformation> BESsToUpdate = new LinkedList<BESInformation>();
		Date now = new Date();
		
		synchronized(_besWorkerLock)
		{
			for (BESInformation info : _besByID.values())
			{
				synchronized(info)
				{
					Date nextUpdate = info.getNextUpdate(_BES_UPDATE_CYCLE, 10);
					if (!now.before(nextUpdate))
						BESsToUpdate.add(info);
				}
			}
		}
		
		updateBESInformation(BESsToUpdate);
	}
	
	private void updateBESInformation(Collection<BESInformation> BESs)
		throws SQLException, ResourceException
	{
		_logger.debug("In updateBESInformation(Collection).");
		ICallingContext callingContext = null;
		
		for (BESInformation info : BESs)
		{
			long id;
			String name;
			
			synchronized(info)
			{
				id = info.getResourceID();
				name = info.getResourceName();
			}
			
			EndpointReferenceType epr = getBESEPR(id);
			if (epr != null)
			{
				if (callingContext == null)
					callingContext = getCallingContext();
				
				_outcallThreadPool.enqueue(new BESUpdateHandler(info, epr, callingContext));
			} else
			{
				synchronized(_besWorkerLock)
				{
					_besByID.remove(new Long(id));
					_besByName.remove(name);
					_scheduleableBESs.remove(new Long(id));
				}
			}
		}
	}
	
	private ICallingContext getCallingContext() throws SQLException, ResourceException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement(
				"SELECT propvalue FROM properties WHERE resourceid = ? AND propname = ?");
			stmt.setString(1, _queueid);
			stmt.setString(2, IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME);
			rs = stmt.executeQuery();
			
			if (rs.next())
				return (ICallingContext)DBSerializer.fromBlob(rs.getBlob(1));
			return new CallingContextImpl((CallingContextImpl)null);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ResourceException("Unable to retrieve calling context.", cnfe);
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to retrieve calling context.", ioe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	private EndpointReferenceType getBESEPR(long id) throws SQLException, ResourceException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			connection = _connectionPool.acquire();
			stmt = connection.prepareStatement("SELECT resourceendpoint FROM q2resources WHERE resourceid = ?");
			stmt.setLong(1, id);
			rs = stmt.executeQuery();
			
			if (rs.next())
			{
				return EPRUtils.fromBlob(rs.getBlob(1));
			} else
			{
				// It's no longer in the database
				_logger.debug("Found a resource which is no longer in the database.");
				return null;
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	private void raiseSchedulingOpportunity()
	{
		_logger.info("If we were fully implemented, we'd raise a scheduling opportunity now.");
		// TODO
	}
	
	private void makeAvailable(long id, String name, BESInformation info)
	{
		boolean needSchedulingOpportunity = false;
		
		synchronized(_besWorkerLock)
		{
			synchronized(info)
			{
				if (!info.isResponsive())
					needSchedulingOpportunity = info.slotsAvailable() > 0;
				
				info.update(true);
			}
			
			if (needSchedulingOpportunity)
				_scheduleableBESs.put(new Long(id), info);
		}
		
		if (needSchedulingOpportunity)
			raiseSchedulingOpportunity();
	}
	
	private void makeUnavailable(long id, String name, BESInformation info)
	{
		synchronized(_besWorkerLock)
		{
			synchronized(info)
			{
				info.update(false);
			}

			_scheduleableBESs.remove(new Long(id));
		}
	}
	
	private class BESUpdaterWorker implements Runnable
	{
		public void run()
		{
			try
			{
				updateBESInformation();
			}
			catch (Throwable sqe)
			{
				_logger.error("Unable to update initial BES information.", sqe);
			}
			
			while (!_closed)
			{
				try
				{
					Collection<BESInformation> newBESs = null;
					Date nextUpdate = new Date(System.currentTimeMillis() + _BES_UPDATE_CYCLE/10);
					
					while (newBESs == null && new Date().before(nextUpdate))
					{
						synchronized(_newBESs)
						{
							if (_newBESs.isEmpty())
							{
								long timeout = nextUpdate.getTime() - System.currentTimeMillis();
								timeout /= 10;
								
								if (timeout <= 0)
									timeout = 1;
								_newBESs.wait(timeout);
							} else
							{
								newBESs = new ArrayList<BESInformation>(_newBESs);
								_newBESs.clear();
							}
						}
						
						if (newBESs != null && !newBESs.isEmpty())
						{
							synchronized(_besWorkerLock)
							{
								for (BESInformation info : newBESs)
								{
									synchronized(info)
									{
										_besByID.put(new Long(info.getResourceID()), info);
										_besByName.put(info.getResourceName(), info);
									}
								}
							}
							
							updateBESInformation(newBESs);
							newBESs = null;
						}
					}
					
					updateBESInformation();
				}
				catch (InterruptedException ie)
				{
					Thread.interrupted();
				}
				catch (Throwable cause)
				{
					_logger.warn("Worker thread threw exception.", cause);
				}
			}
		}
	}
	
	private class BESUpdateHandler implements Runnable
	{
		private EndpointReferenceType _target;
		private ICallingContext _callingContext;
		private BESInformation _besInfo;
		
		public BESUpdateHandler(BESInformation info, EndpointReferenceType target, ICallingContext callingContext)
		{
			_target = target;
			_callingContext = callingContext;
			_besInfo = info;
		}
		
		public void run()
		{
			String name;
			long id;
			
			synchronized(_besInfo)
			{
				name = _besInfo.getResourceName();
				id = _besInfo.getResourceID();
			}
			
			try
			{
				_logger.debug("Making outcall to resource \"" + name + "\" to check it's status.");
				BESPortType bpt = ClientUtils.createProxy(BESPortType.class, _target, _callingContext);
				bpt.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());
				_logger.debug("Marking resource \"" + name + "\" as available.");
				makeAvailable(id, name, _besInfo);
			}
			catch (Throwable cause)
			{
				_logger.debug("Exception occurred trying to talk to BES container -- marking as unavailble.", cause);
				makeUnavailable(id, name, _besInfo);
			}
		}
	}
}