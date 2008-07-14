package edu.virginia.vcgr.genii.container.deployer;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class DeployDatabase implements Closeable
{
	static private Log _logger = LogFactory.getLog(DeployDatabase.class);
	
	static private QName _CONFIGURATION_SECTION =
		new QName("http://vcgr.cs.virginia.edu/Genesis-II",
			"deployment-configuration");
	static private final String _DEPLOYMENT_POOL_PROPERTY =
		"edu.virginia.vcgr.genii.container.deployment.connection-pool";
	
	static private final String[] _TABLE_CREATION_STMTS =
	{
		"CREATE TABLE DEPLOYMENTS (" +
			"INSTANCE_ID VARCHAR(512) PRIMARY KEY," +
			"DEPLOYMENT_ID VARCHAR(512)," +
			"DIRECTORY_NAME VARCHAR(128)," +
			"LAST_ACCESSED TIMESTAMP," +
			"USE_COUNT INTEGER," +
			"STATE VARCHAR(32))",
		"CREATE TABLE DEPLOYMENT_STAMPS (" +
			"INSTANCE_ID VARCHAR(512)," +
			"COMPONENT_ID VARCHAR(512)," +
			"MODIFICATION_TIME TIMESTAMP)"
	};
	
	/* Hypersonic
	static private final String _FIND_STALE_INSTANCES_TEXT =
		"SELECT INSTANCE_ID, DIRECTORY_NAME FROM DEPLOYMENTS WHERE " +
			"(((USE_COUNT = 0) AND ((STATE = ?) OR " +
			"(DATEDIFF('ms', LAST_ACCESSED, CURRENT_TIMESTAMP) > ?)))" +
			"OR(STATE = ?))";
	*/
	static private final String _FIND_STALE_INSTANCES_TEXT =
		"SELECT INSTANCE_ID, DIRECTORY_NAME FROM DEPLOYMENTS WHERE " +
			"(((USE_COUNT = 0) AND ((STATE = ?) OR " +
			"({fn TIMESTAMPDIFF(SQL_TSI_DAY, LAST_ACCESSED, CURRENT_TIMESTAMP)} > ?)))" +
			"OR(STATE = ?))";
	static private final String _SET_STATE_TEXT =
		"UPDATE DEPLOYMENTS SET STATE = ? WHERE INSTANCE_ID = ?";
	static private final String _DELETE_DEPLOYMENTS_TEXT =
		"DELETE FROM DEPLOYMENTS WHERE INSTANCE_ID = ?";
	static private final String _DELETE_STAMPS_TEXT =
		"DELETE FROM DEPLOYMENT_STAMPS WHERE INSTANCE_ID = ?";
	static private final String _FIND_KNOWN_DEPLOYMENTS_TEXT =
		"SELECT INSTANCE_ID, COMPONENT_ID, MODIFICATION_TIME FROM " +
			"DEPLOYMENT_STAMPS";
	static private final String _CREATE_DEPLOYMENT_TEXT =
		"INSERT INTO DEPLOYMENTS VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
	static private final String _CREATE_STAMP_TEXT =
		"INSERT INTO DEPLOYMENT_STAMPS VALUES (?, ?, ?)";
	static private final String _UPDATE_COUNT_TEXT =
		"UPDATE DEPLOYMENTS SET LAST_ACCESSED = CURRENT_TIMESTAMP, " +
			"USE_COUNT = (" +
				"(SELECT USE_COUNT FROM DEPLOYMENTS WHERE INSTANCE_ID = ?)" +
				" + ?) WHERE INSTANCE_ID = ?";
	static private final String _GET_DIRECTORY_TEXT =
		"SELECT DIRECTORY_NAME FROM DEPLOYMENTS WHERE INSTANCE_ID = ?";
	
	static private DatabaseConnectionPool _pool = null;
	
	private Connection _connection = null;
	
	private PreparedStatement _findStaleInstances = null;
	private PreparedStatement _setState = null;
	private PreparedStatement _deleteDeployments = null;
	private PreparedStatement _deleteStamps = null;
	private PreparedStatement _findKnownDeployments = null;
	private PreparedStatement _createDeployment = null;
	private PreparedStatement _createStamp = null;
	private PreparedStatement _updateCount = null;
	private PreparedStatement _getDirectory = null;
	
	static 
	{
		try
		{
			_pool = getConnectionPool();

			createTables();
		}
		catch (Exception e)
		{
			_logger.error("Unable to create deployment database propertly", e);
			throw new RuntimeException("Can't create deployment database.",
				e);
		}
	}
	
	public DeployDatabase() throws SQLException
	{
		_connection = _pool.acquire();
		
		prepareStatements();
	}
	
	protected void finalize() throws Throwable
	{
		try
		{
			close();
		}
		finally
		{
			super.finalize();
		}
	}
	
	public void close() throws IOException
	{
		synchronized(this)
		{
			StreamUtils.close(_findStaleInstances);
			StreamUtils.close(_setState);
			StreamUtils.close(_deleteDeployments);
			StreamUtils.close(_deleteStamps);
			StreamUtils.close(_findKnownDeployments);
			StreamUtils.close(_createDeployment);
			StreamUtils.close(_createStamp);
			StreamUtils.close(_updateCount);
			StreamUtils.close(_getDirectory);
			
			_pool.release(_connection);
			_connection = null;
		}
	}
	
	private void prepareStatements() throws SQLException
	{
		_findStaleInstances = _connection.prepareStatement(
			_FIND_STALE_INSTANCES_TEXT);
		_findStaleInstances.setString(1, 
			DeploymentState.STALE.name());
		_findStaleInstances.setString(3,
			DeploymentState.PARTIAL.name());
		
		_setState = _connection.prepareStatement(_SET_STATE_TEXT);
		_deleteDeployments = _connection.prepareStatement(
			_DELETE_DEPLOYMENTS_TEXT);
		_deleteStamps = _connection.prepareStatement(_DELETE_STAMPS_TEXT);
		_findKnownDeployments = _connection.prepareStatement(
			_FIND_KNOWN_DEPLOYMENTS_TEXT);
		
		_createDeployment = _connection.prepareStatement(
			_CREATE_DEPLOYMENT_TEXT);
		_createStamp = _connection.prepareStatement(_CREATE_STAMP_TEXT);
		
		_updateCount = _connection.prepareStatement(
			_UPDATE_COUNT_TEXT);
		
		_getDirectory = _connection.prepareStatement(_GET_DIRECTORY_TEXT);
	}
	
	public void commit() throws SQLException
	{
		_connection.commit();
	}
	
	public void rollback() throws SQLException
	{
		_connection.rollback();
	}
	
	public Collection<DeploymentInformation>
		retrieveStaleDeployments(long timeoutDays) throws SQLException
	{
		// keep in mind that stale means:
		//		USE_COUNT == 0 && LAST_ACCESSED > timeout
		//		USE_COUNT == 0 && STALE
		//		STATE == PARTIAL
		
		ResultSet rs = null;
		try
		{
			synchronized(_findStaleInstances)
			{
				_findStaleInstances.setLong(2, timeoutDays);
				rs = _findStaleInstances.executeQuery();
			}
			
			ArrayList<DeploymentInformation> ret = 
				new ArrayList<DeploymentInformation>();
			while (rs.next())
			{
				ret.add(new DeploymentInformation(
					rs.getString(1), rs.getString(2)));
			}
			
			return ret;
		}
		finally
		{
			StreamUtils.close(rs);
		}
	}
	
	public void setState(String instanceID, DeploymentState state)
		throws SQLException
	{
		int result;
		synchronized(_setState)
		{
			_setState.setString(1, state.name());
			_setState.setString(2, instanceID);
			result = _setState.executeUpdate();
		}
		
		if (result != 1)
			throw new SQLException("Unable to update state for instance "
				+ instanceID);
	}
	
	public void deleteDeployment(String instanceID) throws SQLException
	{
		_deleteDeployments.setString(1, instanceID);
		_deleteStamps.setString(1, instanceID);
		
		_deleteDeployments.executeUpdate();
		_deleteStamps.executeUpdate();
	}
	
	public HashMap<DeploySnapshot, String> getKnownDeployments()
		throws SQLException
	{
		ResultSet rs = null;
		HashMap<String, Collection<DeployFacet>> tmp =
			new HashMap<String, Collection<DeployFacet>>();
		
		try
		{
			rs = _findKnownDeployments.executeQuery();
			while (rs.next())
			{
				String instanceID = rs.getString(1);
				String componentID = rs.getString(2);
				Timestamp modTime = rs.getTimestamp(3);
				
				Collection<DeployFacet> facet = tmp.get(instanceID);
				if (facet == null)
					tmp.put(instanceID, (facet = new ArrayList<DeployFacet>()));
				facet.add(new DeployFacet(componentID, modTime));
			}
			
			HashMap<DeploySnapshot, String> ret =
				new HashMap<DeploySnapshot, String>(tmp.size());
			for (String instanceID : tmp.keySet())
			{
				Collection<DeployFacet> facets = tmp.get(instanceID);
				ret.put(new DeploySnapshot(facets), instanceID);
			}
			return ret;
		}
		finally
		{
			StreamUtils.close(rs);
		}
	}
	
	public String createDeployment(String deploymentID,
		String deployDirectory, DeploySnapshot snapshot)
			throws SQLException
	{
		String instanceID = new GUID().toString();
		_createDeployment.setString(1, instanceID);
		_createDeployment.setString(2, deploymentID);
		_createDeployment.setString(3, deployDirectory);
		_createDeployment.setInt(4, 0);
		_createDeployment.setString(5, DeploymentState.PARTIAL.name());
		
		if (_createDeployment.executeUpdate() != 1)
			throw new SQLException("Unable to add row to deployments table.");
		
		for (DeployFacet facet : snapshot)
		{
			_createStamp.setString(1, instanceID);
			_createStamp.setString(2, facet.getComponentID());
			_createStamp.setTimestamp(3, facet.getLastModified());

			if (_createStamp.executeUpdate() != 1)
				throw new SQLException("Unable to add row to stamps table.");
		}
		
		return instanceID;
	}
	
	public void updateCount(String instanceID, int delta)
		throws SQLException
	{
		_updateCount.setString(1, instanceID);
		_updateCount.setInt(2, delta);
		_updateCount.setString(3, instanceID);
		
		if (_updateCount.executeUpdate() != 1)
			throw new SQLException("Unable to update counts.");
	}
	
	public String getDirectory(String instanceID) throws SQLException
	{
		ResultSet rs = null;
		
		try
		{
			_getDirectory.setString(1, instanceID);
			rs = _getDirectory.executeQuery();
			
			if (!rs.next())
				throw new SQLException("Unable to find matching instance.");
			
			return rs.getString(1);
		}
		finally
		{
			StreamUtils.close(rs);
		}
	}
	
	static private DatabaseConnectionPool getConnectionPool()
	{
		XMLConfiguration xmlConf =
			ConfigurationManager.getCurrentConfiguration().getContainerConfiguration();
		Properties properties = 
			(Properties)xmlConf.retrieveSection(_CONFIGURATION_SECTION);
		String instanceName = properties.getProperty(_DEPLOYMENT_POOL_PROPERTY);
		return getConnectionPool(instanceName);
	}
	
	static private DatabaseConnectionPool getConnectionPool(
		String poolName)
	{
		DatabaseConnectionPool pool = null;
		Object obj = NamedInstances.getServerInstances().lookup(poolName);
		if (obj != null)
		{
			pool = (DatabaseConnectionPool)obj;
			return pool;
		}
		
		throw new ConfigurationException("Couldn't find connection pool \"" +
			poolName + "\".");
	}
	
	static private void createTables() throws SQLException
	{
		Connection connection = null;
		Statement stmt = null;
		
		try
		{
			connection = _pool.acquire();
			stmt = connection.createStatement();
			
			for (String creationText : _TABLE_CREATION_STMTS)
			{
				try
				{
					stmt.executeUpdate(creationText);
				}
				catch (SQLException sqe)
				{
					// It's probably just already in the database.
				}
			}
			
			connection.commit();
		}
		catch (SQLException sqe)
		{
			if (sqe.getErrorCode() != -21)
				throw sqe;
		}
		finally
		{
			StreamUtils.close(stmt);
			
			_pool.release(connection);
		}
	}
}