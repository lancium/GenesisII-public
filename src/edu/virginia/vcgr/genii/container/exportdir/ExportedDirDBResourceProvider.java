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
		try
		{
			return new ExportedDirDBResourceFactory(pool);
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.getLocalizedMessage(), ioe);
		}
		catch (ConfigurationException ce)
		{
			throw new ResourceException(ce.getLocalizedMessage(), ce);
		}
	}
}