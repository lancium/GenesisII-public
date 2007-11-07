package edu.virginia.vcgr.genii.container.informationService.resource;

import java.sql.SQLException;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.rns.RNSDBResourceProvider;

public class ISDBResourceProvider extends RNSDBResourceProvider{

	public ISDBResourceProvider(Properties properties) 
		throws SQLException, ConfigurationException 
	{
		super(properties);
		
	}

	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
	throws SQLException
	{
		return new DBISResourceFactory(pool);
	}
}
