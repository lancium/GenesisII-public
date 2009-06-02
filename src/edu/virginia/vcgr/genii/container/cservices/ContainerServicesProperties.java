package edu.virginia.vcgr.genii.container.cservices;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;

public class ContainerServicesProperties
{
	static private Log _logger = LogFactory.getLog(
		ContainerServicesProperties.class);
	
	static private class ListenerBundle
	{
		private Pattern _filter;
		private ContainerServicePropertyListener _listener;
		
		private ListenerBundle(Pattern filter, 
			ContainerServicePropertyListener listener)
		{
			if (listener == null)
				throw new IllegalArgumentException(
					"Container listener cannot be null.");
			
			_filter = filter;
			_listener = listener;
		}
		
		public boolean equals(ListenerBundle bundle)
		{
			if (_filter == null)
			{
				if (bundle._filter != null)
					return false;
			} else
			{
				if (bundle._filter == null)
					return false;
				
				if (!_filter.pattern().equals(bundle._filter.pattern()))
					return false;
			}
			
			return _listener.equals(bundle._listener);
		}
		
		@Override
		public boolean equals(Object other)
		{
			if (other instanceof ListenerBundle)
				return equals((ListenerBundle)other);
			
			return false;
		}
	}
	
	static private void prepareDatabase(DatabaseConnectionPool connectionPool)
	{
		Connection connection = null;
		
		try
		{
			connection = connectionPool.acquire(false);
			DatabaseTableUtils.createTables(connection, false, 
				"CREATE TABLE containerservicesproperties (" +
					"name VARCHAR(256) PRIMARY KEY, " +
					"value BLOB(2G) NOT NULL)");
			connection.commit();
		}
		catch (SQLException sqe)
		{
			_logger.error(
				"Cannot create container services properties table.", sqe);
		}
		finally
		{
			connectionPool.release(connection);
		}
	}
	
	private DatabaseConnectionPool _connectionPool;
	private Collection<ListenerBundle> _listeners =
		new LinkedList<ListenerBundle>();
	
	protected void firePropertyChanged(
		String propertyName, Serializable newValue)
	{
		Collection<ContainerServicePropertyListener> listeners =
			new LinkedList<ContainerServicePropertyListener>();
		
		synchronized(_listeners)
		{
			for (ListenerBundle bundle : _listeners)
			{
				Pattern p = bundle._filter;
				if (p != null)
				{
					if (!p.matcher(propertyName).matches())
						continue;
				}
				
				listeners.add(bundle._listener);
			}
		}
		
		for (ContainerServicePropertyListener listener : listeners)
		{
			try
			{
				listener.propertyChanged(propertyName, newValue);
			}
			catch (Throwable cause)
			{
				_logger.error(String.format(
					"Container service property listener \"%s\" threw exception.",
					listener), cause);
			}
		}
	}
	
	public ContainerServicesProperties(DatabaseConnectionPool connectionPool)
	{
		_connectionPool = connectionPool;
		
		prepareDatabase(_connectionPool);
	}
	
	public void addPropertyChangeListener(Pattern filter, 
		ContainerServicePropertyListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(new ListenerBundle(filter, listener));
		}
	}
	
	public void addPropertyChangeListener(
		ContainerServicePropertyListener listener)
	{
		addPropertyChangeListener(null, listener);
	}
	
	public void removePropertyChangeListener(Pattern filter,
		ContainerServicePropertyListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(new ListenerBundle(filter, listener));
		}
	}
	
	public void removePropertyChangeListener(
		ContainerServicePropertyListener listener)
	{
		removePropertyChangeListener(null, listener);
	}
	
	public void setProperty(String propertyName, Serializable newValue)
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		if (newValue == null)
		{
			removeProperty(propertyName);
			return;
		}
		
		try
		{
			connection = _connectionPool.acquire(false);
			stmt = connection.prepareStatement(
				"SELECT name FROM containerservicesproperties WHERE name = ?");
			stmt.setString(1, propertyName);
			rs = stmt.executeQuery();
			
			if (rs.next())
			{
				stmt.close();
				stmt = null;
				
				stmt = connection.prepareStatement(
					"UPDATE containerservicesproperties " +
					"SET value = ? WHERE name = ?");
				stmt.setBlob(1, DBSerializer.toBlob(newValue,
					"containerservicesproperties", "value"));
				stmt.setString(2, propertyName);
				if (stmt.executeUpdate() != 1)
					throw new SQLException(
						"Unable to update property in database.");
			} else
			{
				stmt.close();
				stmt = null;
				
				stmt = connection.prepareStatement(
					"INSERT INTO containerservicesproperties (name, value) " +
					"VALUES (?, ?)");
				stmt.setString(1, propertyName);
				stmt.setBlob(2, DBSerializer.toBlob(newValue,
					"containerservicesproperties", "value"));
				if (stmt.executeUpdate() != 1)
					throw new SQLException(
						"Unable to insert property into database.");
			}
			
			connection.commit();
			firePropertyChanged(propertyName, newValue);
		}
		catch (SQLException sqe)
		{
			_logger.error("Unable to set property in database.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	public void removeProperty(String propertyName)
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement(
				"DELETE FROM containerservicesproperties WHERE name = ?");
			stmt.setString(1, propertyName);
			
			if (stmt.executeUpdate() != 1)
				throw new SQLException(
					"Unable to remove property from database.");
			
			connection.commit();
			firePropertyChanged(propertyName, null);
		}
		catch (SQLException sqe)
		{
			_logger.error("Unable to remove property from database.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
	
	public Serializable getProperty(String propertyName)
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			stmt = connection.prepareStatement(
				"SELECT value FROM containerservicesproperties " +
					"WHERE name = ?");
			stmt.setString(1, propertyName);
			
			rs = stmt.executeQuery();
			if (!rs.next())
				return null;
			return (Serializable)DBSerializer.fromBlob(rs.getBlob(1));
		}
		catch (SQLException sqe)
		{
			_logger.error("Unable to get property from database.", sqe);
			return null;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
	}
}