package edu.virginia.vcgr.genii.container.replicatedExport;

import java.sql.SQLException;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class RExportDBResourceProvider extends BasicDBResourceProvider
{
	public RExportDBResourceProvider(Properties props)
		throws ConfigurationException, SQLException
	{
		super(props);
	}
	
	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException, ResourceException
	{
		return new RExportDBResourceFactory(pool, getTranslater());
	}
}