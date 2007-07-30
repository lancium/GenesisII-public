package edu.virginia.vcgr.genii.container.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class HypersonicDatabaseConnectionPool extends DatabaseConnectionPool
{
	public HypersonicDatabaseConnectionPool(Properties connectionProperties)
		throws IllegalAccessException, ClassNotFoundException, InstantiationException
	{
		super(connectionProperties);
	}
	
	protected Connection createConnection() throws SQLException
	{
		Connection conn = super.createConnection();
		
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("SET WRITE_DELAY FALSE");
			conn.commit();
			
			return conn;
		}
		finally
		{
			if (stmt != null)
				try { stmt.close(); } catch (Throwable t) {}
		}
	}
}