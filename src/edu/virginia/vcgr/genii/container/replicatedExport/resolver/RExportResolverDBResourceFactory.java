package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class RExportResolverDBResourceFactory extends BasicDBResourceFactory implements
IResourceFactory
{
	public RExportResolverDBResourceFactory(
			DatabaseConnectionPool pool)
		throws SQLException
	{
		super(pool);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new RExportResolverDBResource(parentKey, _pool);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
					"Unable to instantiate RExportResolver resource", sqe);
		}
	}	
}