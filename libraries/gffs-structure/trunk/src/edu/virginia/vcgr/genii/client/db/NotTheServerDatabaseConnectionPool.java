package edu.virginia.vcgr.genii.client.db;

import java.util.Properties;

public abstract class NotTheServerDatabaseConnectionPool extends DatabaseConnectionPool
{
	public NotTheServerDatabaseConnectionPool(Properties connectionProperties, String specialString)
		throws IllegalAccessException, ClassNotFoundException, InstantiationException
	{
		super(new DatabaseConnectionPool.DBPropertyNames(), connectionProperties, specialString);
	}
}
