package edu.virginia.vcgr.genii.container.exportdir;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class ExportedRootDBResourceFactory extends ExportedDirDBResourceFactory {
	public ExportedRootDBResourceFactory(ServerDatabaseConnectionPool pool)
			throws SQLException {
		super(pool);
	}

	public IResource instantiate(ResourceKey parentKey)
			throws ResourceException {
		try {
			return new ExportedRootDBResource(parentKey, _pool);
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
}