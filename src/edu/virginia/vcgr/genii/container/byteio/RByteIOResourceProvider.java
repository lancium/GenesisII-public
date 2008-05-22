package edu.virginia.vcgr.genii.container.byteio;

import java.sql.SQLException;
import java.util.Properties;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class RByteIOResourceProvider extends BasicDBResourceProvider
{
	public RByteIOResourceProvider(Properties props)
		throws SQLException
	{
		super(props);
	}

	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException, ResourceException
	{
		return new RByteIOResourceFactory(pool, getTranslater());
	}
}
