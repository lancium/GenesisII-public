package edu.virginia.vcgr.genii.container.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

public class DatabaseTableUtils
{
	static private Log _logger = LogFactory.getLog(DatabaseTableUtils.class);

	static public void createTables(Connection connection, boolean failOnExists, String... tableStatements) throws SQLException
	{
		Statement stmt = null;
		SQLException catchFail = null;
		try {
			stmt = connection.createStatement();

			for (String tableStatement : tableStatements) {
				try {
					stmt.executeUpdate(tableStatement);
				} catch (SQLException sqe) {
					// In the X/Open standard, 42S01 is "table exists" and 42S11 is "index exists".
					// X0Y32 is a derby-specific error state.
					// 42000 is "access denied", but MySQL returns it for "index exists".
					if (sqe.getSQLState().equals("X0Y32") || sqe.getSQLState().equals("42S01")
						|| sqe.getSQLState().equals("42S11")
						|| (sqe.getSQLState().equals("42000") && tableStatement.toUpperCase().startsWith("CREATE INDEX "))) {
						// The table already exists.
						if (failOnExists) {
							catchFail = sqe;
							break;
						} else {
							if (_logger.isDebugEnabled())
								_logger.debug("Received an SQL Exception for a table " + "that already exists.", sqe);
						}
					} else {
						catchFail = sqe;
						break;
					}
				}
			}
			if (catchFail != null) {
				throw catchFail;
			}
		} finally {
			StreamUtils.close(stmt);
		}
	}

	static public void addColumns(Connection connection, boolean failOnExists, String... addColumnStatements)
		throws SQLException
	{
		Statement stmt = null;
		SQLException catchFail = null;
		try {
			stmt = connection.createStatement();
			for (String updateStatement : addColumnStatements) {
				try {
					stmt.executeUpdate(updateStatement);
				} catch (SQLException sqe) {
					if (sqe.getSQLState().equals("X0Y32")) {
						if (failOnExists) {
							catchFail = sqe;
							break;
						} else {
							if (_logger.isDebugEnabled())
								_logger.debug("Received an SQL Exception for a column that already exists.", sqe);
						}
					} else {
						sqe = catchFail;
						break;
					}
				}
			}
			if (catchFail != null)
				throw catchFail;
		} finally {
			StreamUtils.close(stmt);
		}
	}
}