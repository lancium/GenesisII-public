package edu.virginia.vcgr.genii.container.bes;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;;

public class SharedBESTableBase extends BasicDBResourceFactory
{
	static private final String _CREATE_ACTIVITY_ENTRY_TABLE_STMT =
		"CREATE TABLE besactivities (containerkey VARCHAR(128)," +
		" activitykey VARCHAR(128) PRIMARY KEY, activityepr VARCHAR(8192) FOR BIT DATA)";
	
	protected SharedBESTableBase(DatabaseConnectionPool pool)
		throws SQLException
	{
		super(pool);
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
			
			stmt.executeUpdate(_CREATE_ACTIVITY_ENTRY_TABLE_STMT);
			conn.commit();
		}
		catch (SQLException sqe)
		{
//			 assume the table already exists.
		}
		finally
		{
			if (stmt != null)
				try { stmt.close(); } catch (SQLException sqe) {}
			if (conn != null)
				_pool.release(conn);
		}
	}
}