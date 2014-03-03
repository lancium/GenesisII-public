package edu.virginia.vcgr.genii.container.iterator.resource;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class WSIteratorDBResourceProvider extends BasicDBResourceProvider
{
	@Override
	protected WSIteratorDBResourceFactory instantiateResourceFactory(ServerDatabaseConnectionPool pool) throws SQLException
	{
		return new WSIteratorDBResourceFactory(pool);
	}
}