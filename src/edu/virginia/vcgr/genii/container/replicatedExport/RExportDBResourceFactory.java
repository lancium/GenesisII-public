package edu.virginia.vcgr.genii.container.replicatedExport;

import java.sql.SQLException;


import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class RExportDBResourceFactory extends SharedRExportBaseFactory
{
	public RExportDBResourceFactory(
			DatabaseConnectionPool pool)
		throws SQLException
	{
		super(pool);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try{
			return new RExportDBResource(parentKey, _pool);
		}
		catch (SQLException sqe){
			throw new ResourceException(
					"Could not create rexport db resource.", sqe);
		}
	}
}