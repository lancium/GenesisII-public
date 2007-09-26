package edu.virginia.vcgr.genii.container.certGenerator;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class CertGeneratorDBResourceFactory extends BasicDBResourceFactory implements
		IResourceFactory
{
	public CertGeneratorDBResourceFactory(DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(connectionPool);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new CertGeneratorDBResource(parentKey, _pool);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
}