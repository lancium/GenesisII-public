package edu.virginia.vcgr.genii.container.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.BESPortType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.utils.BoundedBlockingQueue;
import edu.virginia.vcgr.genii.client.utils.BoundedThreadPool;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class ResourceInfoManager implements Runnable
{
	static private Log _logger = LogFactory.getLog(ResourceInfoManager.class);
	
	static private final long _DEFAULT_UPDATE_CYCLE = 1000L * 60 * 5; // 5 minutes
	static private final int _NUM_OUTSTANDING_THREADS = 8;
	
	static private DatabaseConnectionPool _connectionPool = null;
	static private HashMap<String, ResourceInfoManager> _managers =
		new HashMap<String, ResourceInfoManager>();
	
	static final private String _FIND_QUEUES_STMT =
		"SELECT queueid FROM queueresources GROUP BY queueid";
	static public void startAllManagers(DatabaseConnectionPool connectionPool)
		throws SQLException, ResourceException
	{
		_connectionPool = connectionPool;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			conn = _connectionPool.acquire();
			stmt = conn.prepareStatement(_FIND_QUEUES_STMT);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				String queueid = rs.getString(1);
				_logger.info("Starting queue manager for queue " + queueid);
				createManager(queueid);
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(conn);
		}
	}
	
	static public ResourceInfoManager getManager(String queueID)
		throws ResourceException
	{
		ResourceInfoManager manager;
		synchronized(_managers)
		{
			manager = _managers.get(queueID);
		}
		
		if (manager == null)
			manager = createManager(queueID);
		
		return manager;
	}
	
	static public ResourceInfoManager createManager(String queueID)
		throws ResourceException
	{
		ResourceInfoManager manager;
		synchronized(_managers)
		{
			manager = _managers.get(queueID);
			if (manager != null)
				throw new ResourceException("Resource manager for queue \""
					+ queueID + "\" already exists.");
			manager = new ResourceInfoManager(queueID);
			_managers.put(queueID, manager);
		}
		
		return manager;
	}
	
	static public void deleteManager(String queueID)
		throws ResourceException
	{
		ResourceInfoManager manager;
		synchronized(_managers)
		{
			manager = _managers.remove(queueID);
		}
		
		if (manager == null)
			throw new ResourceException("Couldn't find resource manager for queue \""
				+ queueID + "\".");
		manager.destroy();
	}
	
	private String _queueID;
	private Thread _myThread;
	volatile private boolean _finished = false;
	private BoundedThreadPool _threadPool = null;
	
	private ResourceInfoManager(String queueID)
	{
		_threadPool = new BoundedThreadPool(
			new BoundedBlockingQueue<Runnable>(_NUM_OUTSTANDING_THREADS));
		ThreadFactory factory = (ThreadFactory)NamedInstances.getRoleBasedInstances(
		).lookup("thread-factory");
	
		_queueID = queueID;
		_myThread = factory.newThread(this);
		_myThread.setDaemon(false);
		_myThread.setName("Queue Resource Manager");
		_myThread.start();
	}
	
	protected void finalize() throws Throwable
	{
		try
		{
			destroy();
		}
		finally
		{
			super.finalize();
		}
	}
	
	synchronized private void destroy()
	{
		if (_myThread != null)
		{
			_finished = true;
			_myThread.interrupt();
			_myThread = null;
			_threadPool.close();
		}
	}

	public void run()
	{
		while (!_finished)
		{
			try
			{
				_logger.info("Updating resources for queue " + _queueID + ".");
				int numResources = updateResources();
				_logger.info("Finished updating " + numResources + 
					" resources for queue " + _queueID + ".");
				Thread.sleep(_DEFAULT_UPDATE_CYCLE);
			}
			catch (InterruptedException ie)
			{
			}
		}
	}
	
	static private final String _LIST_RESOURCES_STMT =
		"SELECT qri.resourcename, qri.resourceid, qri.endpoint FROM " +
			"(SELECT resourceid FROM queueresources WHERE queueid = ?) AS qr " +
				"INNER JOIN " +
			"(SELECT resourcename, resourceid, endpoint FROM queueresourceinfo " +
				"WHERE totalslots > 0) " +
				"AS qri ON qr.resourceid = qri.resourceid";
	
	private int updateResources()
		throws InterruptedException
	{
		int numResources = 0;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			conn = _connectionPool.acquire();
			stmt = conn.prepareStatement(_LIST_RESOURCES_STMT);
			stmt.setString(1, _queueID);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				int resourceID = rs.getInt(2);
				String resourceName = null;
				EndpointReferenceType resourceEndpoint = null;
				
				try
				{
					resourceEndpoint = EPRUtils.fromBlob(rs.getBlob(3));
					resourceName = rs.getString(1);
				}
				catch (Throwable cause)
				{
				}
				
				updateResource(resourceName, resourceID, resourceEndpoint, null);
				numResources++;
			}
			
			return numResources;
		}
		catch (SQLException exception)
		{
			_logger.error("Unable to read entries from database.", exception);
			return numResources;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(conn);
		}
	}
	
	public void updateResource(String resourceName, int resourceID, 
		EndpointReferenceType resourceEndpoint, Boolean available)
			throws InterruptedException
	{
		_threadPool.enqueue(
			new UpdateWorker(resourceName, resourceID, resourceEndpoint, available));
	}
	
	static private boolean updateResource(EndpointReferenceType endpoint)
	{
		try
		{
			BESPortType bes = ClientUtils.createProxy(BESPortType.class, endpoint);
			GetFactoryAttributesDocumentResponseType resp =
				bes.getFactoryAttributesDocument(
					new GetFactoryAttributesDocumentType());
			return resp.getFactoryResourceAttributesDocument(
				).isIsAcceptingNewActivities();
		}
		catch (Throwable t)
		{
			_logger.debug("Unable to talk to resource -- assuming it's unavailable.");
			return false;
		}
	}
	
	static private final String _UPDATE_RECORD_STMT =
		"UPDATE queuedynamicresourceinfo SET available = ? WHERE resourceid = ?";
	static private final String _ADD_RECORD_STMT =
		"INSERT INTO queuedynamicresourceinfo VALUES (?, ?)";
	
	private void updateRecord(int resourceID, boolean available)
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try
		{
			conn = _connectionPool.acquire();
			stmt = conn.prepareStatement(_UPDATE_RECORD_STMT);
			stmt.setInt(1, available ? 1 : 0);
			stmt.setInt(2, resourceID);
			if (stmt.executeUpdate() != 1)
			{
				stmt.close();
				stmt = null;
				stmt = conn.prepareStatement(_ADD_RECORD_STMT);
				stmt.setInt(1, resourceID);
				stmt.setInt(2, available ? 1 : 0);
				if (stmt.executeUpdate() != 1)
				{
					_logger.error("Unable to update or add a record to the database.");
				}
			}
			conn.commit();
		}
		catch (Throwable t)
		{
			_logger.error("Unable to update records in database.", t);
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(conn);
		}
	}
	
	private class UpdateWorker implements Runnable
	{
		private String _resourceName;
		private int _resourceID;
		private EndpointReferenceType _endpoint;
		private Boolean _available;
		
		public UpdateWorker(String resourceName, 
			int resourceID, EndpointReferenceType endpoint, Boolean available)
		{
			_resourceName = resourceName;
			_resourceID = resourceID;
			_endpoint = endpoint;
			_available = available;
		}
		
		public void run()
		{
			if (_endpoint == null)
			{
				_available = Boolean.FALSE;
			}
			
			if (_available == null)
			{
				_logger.debug("Updating resource " + _resourceName + 
					" for queue " + _queueID);
				_available = new Boolean(
					updateResource(_endpoint));
			}
			
			boolean available = _available.booleanValue();
			_logger.debug("Marking resource " + _resourceName + 
					" for queue " + _queueID + " as " + 
					(available ? "available" : "unavailable"));
			updateRecord(_resourceID, available);
			
			if (available)
			{
				try
				{
					JobManager.getManager(_queueID).jobSchedulingOpportunity();
				}
				catch (ResourceException re)
				{
					_logger.warn(
						"Problem announcing job scheduling opportunity.", re);
				}
			}
		}
	}
}
