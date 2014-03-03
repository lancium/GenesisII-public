package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class RExportResolverFactoryDBResource extends BasicDBResource implements
		IRExportResolverFactoryResource {
	public RExportResolverFactoryDBResource(ResourceKey parentKey,
			ServerDatabaseConnectionPool connectionPool) throws SQLException {
		super(parentKey, connectionPool);
	}
}