package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class RExportResolverFactoryDBResourceFactory extends BasicDBResourceFactory implements
		IResourceFactory
{
	public RExportResolverFactoryDBResourceFactory(
			DatabaseConnectionPool pool, 
			IResourceKeyTranslater translator)
		throws SQLException
	{
		super(pool, translator);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new RExportResolverFactoryDBResource(parentKey, _pool, _translater);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
				"Could not create RExportResolverFactory resource.", sqe);
		}
	}
}