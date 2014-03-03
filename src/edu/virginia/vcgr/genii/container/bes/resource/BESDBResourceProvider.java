package edu.virginia.vcgr.genii.container.bes.resource;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;

public class BESDBResourceProvider extends BasicDBResourceProvider {
	protected IResourceFactory instantiateResourceFactory(
			ServerDatabaseConnectionPool pool) throws SQLException {
		return new DBBESResourceFactory(pool);
	}
}