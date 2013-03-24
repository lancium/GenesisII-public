package edu.virginia.vcgr.genii.container;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;

public class PersistentContainerProperties
{
	static private PersistentContainerProperties _properties = null;

	synchronized static public PersistentContainerProperties getProperties()
	{
		if (_properties == null)
			_properties = new PersistentContainerProperties();

		return _properties;
	}

	private DatabaseConnectionPool _pool;

	private PersistentContainerProperties()
	{
		_pool = (DatabaseConnectionPool) NamedInstances.getServerInstances().lookup("connection-pool");
		if (_pool == null)
			throw new ConfigurationException("Unable to find database connection pool.");

		Connection connection = null;
		try {
			connection = _pool.acquire(false);
			DatabaseTableUtils.createTables(connection, false, "CREATE TABLE containerproperties ("
				+ "propertyname VARCHAR(256) PRIMARY KEY," + "propertyvalue BLOB(2G))");
			connection.commit();
		} catch (SQLException e) {
			throw new ConfigurationException("Unable to initialize container.", e);
		} finally {
			_pool.release(connection);
		}
	}

	public Object getProperty(String propertyName) throws SQLException
	{
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		for (int lcv = 0; lcv < 5; lcv++) {
			try {
				connection = _pool.acquire(true);
				stmt = connection.prepareStatement("SELECT propertyvalue FROM containerproperties " + "WHERE propertyname = ?");
				stmt.setString(1, propertyName);
				rs = stmt.executeQuery();
				if (!rs.next())
					return null;

				Blob blob = rs.getBlob(1);
				if (blob == null)
					return null;

				return DBSerializer.fromBlob(rs.getBlob(1));
			} catch (NullPointerException npe) {
				if (lcv < 4) {
					// Make another attempt
					System.err.println("Making another attempt to read property.");
				} else {
					throw npe;
				}
			} finally {
				StreamUtils.close(rs);
				StreamUtils.close(stmt);
				_pool.release(connection);
			}
		}

		throw new ConfigurationException("Unexpected code hit.");
	}

	public void setProperty(String name, Object value) throws SQLException, IOException
	{
		Connection connection = null;
		PreparedStatement stmt = null;

		try {
			connection = _pool.acquire(false);
			stmt = connection.prepareStatement("DELETE FROM containerproperties WHERE propertyname = ?");
			stmt.setString(1, name);
			stmt.executeUpdate();
			stmt.close();
			stmt = connection.prepareStatement("INSERT INTO containerproperties("
				+ "propertyname, propertyvalue) VALUES (?, ?)");
			stmt.setString(1, name);

			Blob b = DBSerializer.toBlob(value, "containerproperties", "propertyvalue");
			stmt.setBlob(2, b);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update container property \"" + name + "\".");
			connection.commit();
		} finally {
			StreamUtils.close(stmt);
			_pool.release(connection);
		}
	}
}