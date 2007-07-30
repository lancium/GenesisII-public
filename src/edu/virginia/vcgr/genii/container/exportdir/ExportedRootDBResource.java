package edu.virginia.vcgr.genii.container.exportdir;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class ExportedRootDBResource extends ExportedDirDBResource implements
		IExportedRootResource
{
	public ExportedRootDBResource(ResourceKey rKey, DatabaseConnectionPool pool)
		throws SQLException
	{
		super(rKey, pool);
	}
}