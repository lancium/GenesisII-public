package edu.virginia.vcgr.genii.container.exportdir;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class ExportedRootDBResource extends ExportedDirDBResource implements
		IExportedRootResource {
	public ExportedRootDBResource(ResourceKey parentKey,
			ServerDatabaseConnectionPool connectionPool) throws SQLException {
		super(parentKey, connectionPool);
	}
}