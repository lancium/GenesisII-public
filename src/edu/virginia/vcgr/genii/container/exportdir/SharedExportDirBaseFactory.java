package edu.virginia.vcgr.genii.container.exportdir;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class SharedExportDirBaseFactory extends BasicDBResourceFactory
{
	static private final String _CREATE_EXPORTED_FILE_TABLE_STMT =
		"CREATE TABLE exportedfile (fileid VARCHAR(40) PRIMARY KEY," +
		"path VARCHAR(512), parentIds VARCHAR(4096))";
	static private final String _CREATE_EXPORTED_DIR_TABLE_STMT =
		"CREATE TABLE exporteddir " +
		"(dirid VARCHAR(40) PRIMARY KEY, path VARCHAR(512), parentIds VARCHAR(4096))";
	static private final String _CREATE_EXPORTED_DIR_ENTRY_TABLE_STMT =
		"CREATE TABLE exporteddirentry " +
		"(dirid VARCHAR(40), name VARCHAR(256), endpoint BLOB (128K), " +
		"entryid VARCHAR(40), type CHAR(1), " +
		"CONSTRAINT exporteddirentryconstraint1 PRIMARY KEY (dirid, name))";
	static private final String _CREATE_EXPORTED_DIR_ATTR_TABLE_STMT =
		"CREATE TABLE exportedentryattr " +
		"(entryid VARCHAR(40) PRIMARY KEY, attr VARCHAR (8192) FOR BIT DATA)";
	
	protected SharedExportDirBaseFactory(DatabaseConnectionPool pool)
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
			
			stmt.executeUpdate(_CREATE_EXPORTED_FILE_TABLE_STMT);
			stmt.executeUpdate(_CREATE_EXPORTED_DIR_TABLE_STMT);
			stmt.executeUpdate(_CREATE_EXPORTED_DIR_ENTRY_TABLE_STMT);
			stmt.executeUpdate(_CREATE_EXPORTED_DIR_ATTR_TABLE_STMT);
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