package edu.virginia.vcgr.genii.container.bes.resource;

import java.sql.SQLException;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class BESDBResourceProvider extends BasicDBResourceProvider
{
	public BESDBResourceProvider(Properties props)
		throws ConfigurationException, SQLException
	{
		super(props);
	}
	
	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException
	{
		return new DBBESResourceFactory(pool);
	}
}