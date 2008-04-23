package edu.virginia.vcgr.genii.container.iterator;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.q2.resource.QueueDBResourceFactory;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class IteratorDBResourceFactory extends BasicDBResourceFactory
{
	static private Log _logger = LogFactory.getLog(QueueDBResourceFactory.class);
	
	static private final String []_CREATE_STMTS = new String[] {
		"CREATE TABLE iterators (" +
			"entryid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
			"iteratorid VARCHAR(256) NOT NULL, " +
			"elementindex BIGINT NOT NULL, " +
			"contents BLOB(128K) NOT NULL, " +
			"CONSTRAINT iteratorsuniqueconstraint UNIQUE (iteratorid, elementindex))"
	};
	
	public IteratorDBResourceFactory(
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
			return new IteratorDBResource(parentKey, _pool, _translater);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
	
	protected void createTables() throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		
		super.createTables();
		
		try
		{
			conn = _pool.acquire();
			stmt = conn.createStatement();
			
			for (String createStmt : _CREATE_STMTS)
			{
				stmt.executeUpdate(createStmt);
			}
			conn.commit();
		}
		catch (SQLException sqe)
		{
			 _logger.debug("Got an exception while creating tables (could be because they exist).", sqe);
		}
		finally
		{
			if (stmt != null)
				try { stmt.close(); } catch (SQLException sqe) {}
			if (conn != null)
				_pool.release(conn);
		}
	}
	
	public DatabaseConnectionPool getConnectionPool()
	{
		return _pool;
	}
}