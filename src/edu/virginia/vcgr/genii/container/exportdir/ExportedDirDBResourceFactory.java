package edu.virginia.vcgr.genii.container.exportdir;

import java.io.IOException;
import java.sql.SQLException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class ExportedDirDBResourceFactory extends SharedExportDirBaseFactory
{
	public ExportedDirDBResourceFactory(DatabaseConnectionPool pool)
		throws SQLException, ConfigurationException, IOException
	{
		super(pool);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new ExportedDirDBResource(parentKey, _pool);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
}