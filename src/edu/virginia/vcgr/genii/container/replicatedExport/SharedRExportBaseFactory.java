package edu.virginia.vcgr.genii.container.replicatedExport;

import java.sql.Connection;
import java.sql.SQLException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class SharedRExportBaseFactory extends BasicDBResourceFactory
{
	static private final String _CREATE_REXPORT_TABLE_STMT =
		"CREATE TABLE rexport " +
		"(dirid VARCHAR(40) PRIMARY KEY, path VARCHAR(512), parentIds VARCHAR(4096))";
	static private final String _CREATE_REXPORT_ENTRY_TABLE_STMT =
		"CREATE TABLE rexportentry " +
		"(dirid VARCHAR(40), name VARCHAR(256), endpoint BLOB (2G), " +
		"entryid VARCHAR(40), type CHAR(1), " +
		"CONSTRAINT rexportentryconstraint1 PRIMARY KEY (dirid, name))";
	static private final String _CREATE_REXPORT_ATTR_TABLE_STMT =
		"CREATE TABLE rexportentryattr " +
		"(entryid VARCHAR(40) PRIMARY KEY, attr VARCHAR (8192) FOR BIT DATA)";
	
	static private final String _CREATE_RESOURCE_TO_RESOLVER_MAPPING_TABLE_STMT =
		"CREATE TABLE resolvermapping " +
		"(resourceEPI VARCHAR(60) PRIMARY KEY, resolverEPI VARCHAR(60), " +
		"resolverEPR BLOB (2G))";
	
	protected SharedRExportBaseFactory(
			DatabaseConnectionPool pool)
		throws SQLException
	{
		super(pool);
	}

	protected void createTables() throws SQLException
	{
		Connection conn = null;
		super.createTables();
		
		try
		{
			conn = _pool.acquire(false);
			DatabaseTableUtils.createTables(conn, false, 
				_CREATE_REXPORT_TABLE_STMT,
				_CREATE_REXPORT_ENTRY_TABLE_STMT,
				_CREATE_REXPORT_ATTR_TABLE_STMT,
				_CREATE_RESOURCE_TO_RESOLVER_MAPPING_TABLE_STMT);
			conn.commit();
		}
		finally
		{
			_pool.release(conn);
		}
	}
}