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
	
	static public void createTables(Connection connection,
		boolean failOnExists, String...tableStatements) throws SQLException
	{
		Statement stmt = null;
		
		try
		{
			stmt = connection.createStatement();
			
			for (String tableStatement : tableStatements)
			{
				try
				{
					stmt.executeUpdate(tableStatement);
				}
				catch (SQLException sqe)
				{
					if (sqe.getSQLState().equals("X0Y32"))
					{
						// The table already exists.
						if (failOnExists)
							throw sqe;
						else
							_logger.debug(
								"Received an SQL Exception for a table " +
								"that already exists.", sqe);
					} else
						throw sqe;
				}
			}
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
}