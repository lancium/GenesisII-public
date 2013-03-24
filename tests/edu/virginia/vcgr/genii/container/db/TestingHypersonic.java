package edu.virginia.vcgr.genii.container.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.junit.Test;
import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class TestingHypersonic
{
	static private final int _NUM_ROWS = 1000;
	static private DatabaseConnectionPool _pool;

	@Test
	public void testNothing()
	{
		// place holder until we resolve the startup issues here.
	}

	static public void main(String[] args) throws Throwable
	{
		// hmmm: currently disabled due to failing when run as unit test.
		boolean fud = true;
		if (fud)
			return;

		createConnectionPool();
		createTables();

		try {
			for (int lcv = 0; lcv < 100 * 1000; lcv++) {
				System.err.println("Iteration " + lcv);

				insertRows();
				queryRows();
				updateRows();
				queryRows();
				deleteRows();

				/*
				 * if (lcv % 50 == 0) { System.err.println("\tCompacting database.");
				 * compactDatabase(); }
				 */
			}
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			throw t;
		} finally {
			dropTables();
		}
	}

	/* This is for hypersonic */
	/*
	 * static void createConnectionPool() throws Throwable { Properties props = new Properties();
	 * props.setProperty("edu.virginia.vcgr.genii.container.db.db-class-name",
	 * "org.hsqldb.jdbcDriver");
	 * props.setProperty("edu.virginia.vcgr.genii.container.db.db-connect-string",
	 * "jdbc:hsqldb:file:/home/mmm2a/marks-database/database");
	 * props.setProperty("edu.virginia.vcgr.genii.container.db.db-user", "sa");
	 * props.setProperty("edu.virginia.vcgr.genii.container.db.db-password", "");
	 * props.setProperty("edu.virginia.vcgr.genii.container.db.pool-size", "8");
	 * 
	 * _pool = new HypersonicDatabaseConnectionPool(props); }
	 */
	static void createConnectionPool() throws Throwable
	{
		Properties props = new Properties();
		props.setProperty("edu.virginia.vcgr.genii.container.db.db-class-name", "org.apache.derby.jdbc.EmbeddedDriver");
		props.setProperty("edu.virginia.vcgr.genii.container.db.db-connect-string",
			"jdbc:derby:C:\\marks-database\\database;create=true");
		props.setProperty("edu.virginia.vcgr.genii.container.db.db-user", "sa");
		props.setProperty("edu.virginia.vcgr.genii.container.db.db-password", "");
		props.setProperty("edu.virginia.vcgr.genii.container.db.pool-size", "8");

		_pool = new DatabaseConnectionPool(props);
	}

	static public void createTables() throws Throwable
	{
		Connection conn = null;
		Statement stmt = null;

		try {
			conn = _pool.acquire(false);
			stmt = conn.createStatement();
			stmt.executeUpdate("CREATE TABLE test1 (ID INTEGER PRIMARY KEY,"
				+ "guid VARCHAR(256), bits VARCHAR (8192) FOR BIT DATA)");
			conn.commit();
		} finally {
			StreamUtils.close(stmt);
			_pool.release(conn);
		}
	}

	static public void dropTables() throws Throwable
	{
		Connection conn = null;
		Statement stmt = null;

		try {
			conn = _pool.acquire(false);
			stmt = conn.createStatement();
			stmt.executeUpdate("DROP TABLE test1 CASCADE");
			conn.commit();
		} finally {
			StreamUtils.close(stmt);
			_pool.release(conn);
		}
	}

	static public void insertRows() throws Throwable
	{
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = _pool.acquire(false);
			stmt = conn.prepareStatement("INSERT INTO test1 VALUES(?, ?, ?)");

			for (int lcv = 0; lcv < _NUM_ROWS; lcv++) {
				stmt.setInt(1, lcv);
				stmt.setString(2, (new GUID()).toString());
				stmt.setBytes(3, new byte[1024]);
				stmt.executeUpdate();
			}

			conn.commit();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			throw t;
		} finally {
			StreamUtils.close(stmt);
			_pool.release(conn);
		}
	}

	static public void queryRows() throws Throwable
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = _pool.acquire(false);
			stmt = conn.prepareStatement("SELECT * FROM test1 WHERE ID > ? AND ID < ?");

			for (int lcv = 100; lcv < (_NUM_ROWS - 100); lcv += 100) {
				stmt.setInt(1, lcv - 10);
				stmt.setInt(2, lcv + 10);

				rs = stmt.executeQuery();

				while (rs.next()) {
					rs.getInt(1);
					rs.getString(2);
					rs.getBytes(3);
				}

				rs.close();
				rs = null;
			}

			conn.commit();
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_pool.release(conn);
		}
	}

	static public void updateRows() throws Throwable
	{
		Connection conn = null;
		Statement stmt = null;

		try {
			conn = _pool.acquire(false);
			stmt = conn.createStatement();

			for (int lcv = 0; lcv < _NUM_ROWS; lcv += 100) {
				for (int i = 0; i < 10; i++) {
					stmt.addBatch("UPDATE test1 SET guid = '" + (new GUID()).toString() + "' WHERE ID = " + (i + lcv));
				}

				stmt.executeBatch();
			}

			conn.commit();
		} finally {
			StreamUtils.close(stmt);
			_pool.release(conn);
		}
	}

	static public void deleteRows() throws Throwable
	{
		Connection conn = null;
		Statement stmt = null;

		try {
			conn = _pool.acquire(false);
			stmt = conn.createStatement();

			stmt.executeUpdate("DELETE FROM test1");
			conn.commit();
		} finally {
			StreamUtils.close(stmt);
			_pool.release(conn);
		}
	}

	static public void compactDatabase() throws Throwable
	{
		/*
		 * Connection conn = null; Statement stmt = null;
		 * 
		 * try { conn = _pool.acquire(); stmt = conn.createStatement();
		 * 
		 * stmt.executeUpdate("SHUTDOWN COMPACT"); conn.commit(); } finally {
		 * StreamUtils.close(stmt); StreamUtils.close(conn); }
		 */
	}
}