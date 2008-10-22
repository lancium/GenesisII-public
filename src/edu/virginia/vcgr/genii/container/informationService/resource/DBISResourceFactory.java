/**
 * @author Krasi
 */

package edu.virginia.vcgr.genii.container.informationService.resource;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.RNSDBResourceFactory;

public class DBISResourceFactory extends RNSDBResourceFactory{
	
	static private Log _logger = LogFactory.getLog(DBISResourceFactory.class);
	
	static private final String _CREATE_BES_ENTRY_TABLE_STMT =
		"CREATE TABLE isbescontainers " +
		"(containerid INTEGER GENERATED BY DEFAULT AS IDENTITY, " +
		"servicekey VARCHAR (128), " +
		"resourcename VARCHAR (128), " +
		"endpoint BLOB (128K), " +
		"serviceEPR BLOB (128K), " +
		"callingcontext BLOB (128K)," +
		"CONSTRAINT isconstraint PRIMARY KEY (containerid))";

	public DBISResourceFactory(
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
			return new DBISResource(parentKey, _pool, _translater);
		}
		catch (SQLException sqe)
		{
			_logger.warn(sqe.getLocalizedMessage(), sqe);
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}

	public DatabaseConnectionPool getConnectionPool()
	{
		return _pool;
	}
	
	protected void createTables() throws SQLException
	{
		Connection conn = null;
		super.createTables();
		 
		try
		{
			conn = _pool.acquire();
			DatabaseTableUtils.createTables(conn, false, 
				_CREATE_BES_ENTRY_TABLE_STMT);
			conn.commit();
			 
		}
		finally
		{
			_pool.release(conn);
		}		
	}
}
