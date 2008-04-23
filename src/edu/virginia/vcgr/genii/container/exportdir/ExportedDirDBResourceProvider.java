package edu.virginia.vcgr.genii.container.exportdir;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class ExportedDirDBResourceProvider extends BasicDBResourceProvider
{
	public ExportedDirDBResourceProvider(Properties props)
		throws ConfigurationException, SQLException
	{
		super(props);
	}
	
	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException, ResourceException
	{
		return new ExportedDirDBResourceFactory(pool, getTranslater());
	}
}