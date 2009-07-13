package edu.virginia.vcgr.genii.container.resource.db;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.common.MatchingParameter;
import edu.virginia.vcgr.genii.container.common.notification.DBSubscriptionResource;
import edu.virginia.vcgr.genii.container.common.notification.SubscriptionInformation;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class BasicDBResource implements IResource
{
	static protected final String _SPECIAL_SERVICE_KEY_TEMPLATE =
		"edu.virginia.vcgr.genii.container.resource.db.special-service-key.";
	
	static private final String _VERIFY_STMT =
		"SELECT createtime FROM resources WHERE resourceid = ?";
	static private final String _CREATE_STMT =
		"INSERT INTO resources VALUES(?, ?)";
	static private final String _REMOVE_PROPERTY_STMT =
		"DELETE FROM properties WHERE resourceid = ? AND propname = ?";
	static private final String _INSERT_PROPERTY_STMT =
		"INSERT INTO properties VALUES (?, ?, ?)";
	static private final String _GET_PROPERTY_STMT =
		"SELECT propvalue FROM properties WHERE resourceid = ? AND propname = ?";
	static private final String _DESTROY_KEYS_STMT =
		"DELETE FROM resources WHERE resourceid = ?";
	static private final String _DESTROY_PROPERTIES_STMT =
		"DELETE FROM properties WHERE resourceid = ?";
	static private final String _DESTROY_MATCHING_PARAMS_STMT =
		"DELETE FROM matchingparams WHERE resourceid = ?";
	
	static private Log _logger = LogFactory.getLog(BasicDBResource.class);
	
	protected DatabaseConnectionPool _connectionPool;
	protected Connection _connection;
	protected String _resourceKey;
	protected ResourceKey _parentKey;

	public BasicDBResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		_parentKey = parentKey;
		_connectionPool = connectionPool;
		_connection = _connectionPool.acquire(false);
	}
	
	public Connection getConnection()
	{
		return _connection;
	}
	
	protected void finalize() throws Throwable
	{
		try
		{
			StreamUtils.close(this);
		}
		finally
		{
			super.finalize();
		}
	}
	
	public String getKey()
	{
		if (_resourceKey.startsWith(_SPECIAL_SERVICE_KEY_TEMPLATE))
			return null;

		return _resourceKey;
	}
	
	public Object getLockKey()
	{
		return _resourceKey;
	}

	public void initialize(HashMap<QName, Object> constructionParams)
		throws ResourceException
	{
		_logger.debug("Initializing resource with construction parameters.");
		Boolean b = (Boolean)constructionParams.get(
			IResource.IS_SERVICE_CONSTRUCTION_PARAM);
		if (b != null && b.booleanValue())
			_resourceKey = _SPECIAL_SERVICE_KEY_TEMPLATE + _parentKey.getServiceName();
		else
			_resourceKey = new GUID().toString();
		
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_CREATE_STMT);
			stmt.setString(1, _resourceKey);
			stmt.setTimestamp(2, new Timestamp(
				new Date().getTime()));
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Couldn't create resource.");
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	public void load(String resourceKey) 
		throws ResourceUnknownFaultType, ResourceException
	{
		_resourceKey = resourceKey;

		if (_resourceKey == null)
			_resourceKey = _SPECIAL_SERVICE_KEY_TEMPLATE + _parentKey.getServiceName();
		
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_VERIFY_STMT);
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			if (!rs.next())
			{
				throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(
					null, null, null, null, new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription("Resource \"" + _resourceKey + "\" is unknown.")
					}, null));
			}
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	public void setProperty(String propertyName, Object value)
		throws ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_REMOVE_PROPERTY_STMT);
			stmt.setString(1, _resourceKey);
			stmt.setString(2, propertyName);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			stmt = _connection.prepareStatement(_INSERT_PROPERTY_STMT);
			stmt.setString(1, _resourceKey);
			stmt.setString(2, propertyName);
			
			Blob b = DBSerializer.toBlob(value,
				"properties", "propvalue");
			if (b != null)
			{
				_logger.debug("Serializing " + b.length() + 
					" bytes into property database.");
				if (b.length() <= 0)
				{
					_logger.error(
						"Attempt to serialize property \"" + propertyName + 
						"\" with 0 bytes into the property database.");
				} else if (b.length() >= 128 * 1024)
				{
					_logger.error("Attempt to serialize property \"" + propertyName +
						"\" of length " + b.length() + " bytes into a "
						+ "128K space.");
				}
			}
			
			stmt.setBlob(3, b);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update property \"" +
					propertyName + "\".");
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to serialize property value.", ioe);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	public Object getProperty(String propertyName) throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = _connection.prepareStatement(_GET_PROPERTY_STMT);
			stmt.setString(1, _resourceKey);
			stmt.setString(2, propertyName);
			rs = stmt.executeQuery();
			if (!rs.next())
				return null;
			
			Blob blob = rs.getBlob(1);
			if (blob == null)
				return null;
			
			return DBSerializer.fromBlob(blob);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to get property.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	public void destroy() throws ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_DESTROY_PROPERTIES_STMT);
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
			stmt.close();
			stmt = _connection.prepareStatement(_DESTROY_KEYS_STMT);
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
			stmt.close();
			stmt = _connection.prepareStatement(_DESTROY_MATCHING_PARAMS_STMT);
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
			
			DBSubscriptionResource.destroySubscriptions(this);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
				"Error while trying to destroy resource.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}

	synchronized public void commit() throws ResourceException
	{
		if (_connection == null)
		{
			// It's already been closed
			return;
		}
		
		try
		{
			_connection.commit();
		}
		catch (SQLException sqe)
		{
			_logger.warn(sqe);
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}

	public void rollback()
	{
		if (_connection == null)
		{
			// It's already been closed.
			return;
		}
		
		try
		{
			_connection.rollback();
		}
		catch (SQLException sqe)
		{
			_logger.error(sqe);
		}
	}

	synchronized public void close() throws IOException
	{
		if (_connection != null)
		{
			_connectionPool.release(_connection);
			_connection = null;
		}
	}

	public ResourceKey getParentResourceKey()
	{
		return _parentKey;
	}
	
	static protected void destroyAll(Connection connection, Collection<String> keys)
		throws ResourceException
	{
		PreparedStatement destroyKeyStmt = null;
		PreparedStatement destroyPropertiesStmt = null;
		
		try
		{
			destroyKeyStmt = connection.prepareStatement(_DESTROY_KEYS_STMT);
			destroyPropertiesStmt = connection.prepareStatement(_DESTROY_PROPERTIES_STMT);
			
			for (String key : keys)
			{
				destroyKeyStmt.setString(1, key);
				destroyKeyStmt.executeUpdate();
					
				destroyPropertiesStmt.setString(1, key);
				destroyPropertiesStmt.executeUpdate();
			}
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			StreamUtils.close(destroyKeyStmt);
			StreamUtils.close(destroyPropertiesStmt);
		}
	}

	public Collection<SubscriptionInformation> matchSubscriptions(
		String topicExpression) throws ResourceException
	{
		return DBSubscriptionResource.matchSubscriptions(
			this, topicExpression);
	}
	
	/**
	 * Return whether or not the resource is a service resource
	 */
	public boolean isServiceResource() {
		if (_resourceKey.startsWith(_SPECIAL_SERVICE_KEY_TEMPLATE)) {
			return true;
		} 
		return false;
	}
	
	@Override
	public Collection<MatchingParameter> getMatchingParameters() throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Collection<MatchingParameter> ret = new LinkedList<MatchingParameter>();
		
		try
		{
			stmt = _connection.prepareStatement(
				"SELECT paramname, paramvalue FROM matchingparams " +
					"WHERE resourceid = ?");
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				ret.add(new MatchingParameter(
					rs.getString(1), rs.getString(2)));
			}
			
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to get matching parameters.",
				sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void addMatchingParameter(MatchingParameter... parameters)
			throws ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(
				"INSERT INTO matchingparams" +
					"(resourceid, paramname, paramvalue) " +
				"VALUES (?, ?, ?)");
			
			for (MatchingParameter param : parameters)
			{
				stmt.setString(1, _resourceKey);
				stmt.setString(2, param.getName());
				stmt.setString(3, param.getValue());
				stmt.addBatch();
			}
			
			stmt.executeBatch();
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to add matching parameters.",
				sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void removeMatchingParameter(MatchingParameter... parameters)
			throws ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(
				"DELETE FROM matchingparams " +
					"WHERE resourceid = ? AND paramname = ? " +
						"AND paramvalue = ?");
			
			for (MatchingParameter param : parameters)
			{
				stmt.setString(1, _resourceKey);
				stmt.setString(2, param.getName());
				stmt.setString(3, param.getValue());
				stmt.addBatch();
			}
			
			stmt.executeBatch();
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to delete matching parameters.",
				sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
}
