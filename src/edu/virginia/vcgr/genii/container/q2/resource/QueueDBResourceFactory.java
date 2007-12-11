package edu.virginia.vcgr.genii.container.q2.resource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class QueueDBResourceFactory extends BasicDBResourceFactory
{
	static private Log _logger = LogFactory.getLog(QueueDBResourceFactory.class);
	
	static private final String []_CREATE_STMTS = new String[] {
		"CREATE TABLE q2resources (" +
			"resourceid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
			"resourcename VARCHAR(256) NOT NULL, " +
			"resourceendpoint BLOB(128K) NOT NULL, " +
			"queueid VARCHAR(256) NOT NULL, totalslots INTEGER NOT NULL," +
			"CONSTRAINT q2resourcesnamecnst UNIQUE (resourcename, queueid))",
		"CREATE TABLE q2jobs (" +
			"jobid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
			"jobticket VARCHAR(256) NOT NULL, queueid VARCHAR(256) NOT NULL, " +
			"callingcontext BLOB(128K) NOT NULL, jsdl BLOB(128K) NOT NULL, " +
			"owners BLOB(128K) NOT NULL," +
			"priority SMALLINT NOT NULL," +
			"state VARCHAR(64) NOT NULL, submittime TIMESTAMP NOT NULL, " +
			"starttime TIMESTAMP, finishtime TIMESTAMP, " +
			"jobendpoint BLOB(128K), resourceid BIGINT, " +
			"resourceendpoint BLOB(128K), " +
			"CONSTRAINT q2jobsticket UNIQUE (jobticket, queueid))"
	};
	
	public QueueDBResourceFactory(DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(connectionPool);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new QueueDBResource(parentKey, _pool);
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