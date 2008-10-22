package edu.virginia.vcgr.genii.container.cservices.scratchmgr;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;

public class ScratchFSDatabase
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(ScratchFSDatabase.class);
	
	public ScratchFSDatabase(Connection conn) throws SQLException
	{
		DatabaseTableUtils.createTables(conn, false,
			"CREATE TABLE swapmgrdirectories (" +
				"dirid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
				"directory VARCHAR(512) UNIQUE NOT NULL, " +
				"lastidlestart TIMESTAMP)",
			"CREATE TABLE swapmgrdirectoryreservations (" +
				"reservationid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
				"dirid BIGINT NOT NULL, timeacquired TIMESTAMP NOT NULL, " +
				"CONSTRAINT swapmgrdirresdiridfk " +
					"FOREIGN KEY (dirid) " +
					"REFERENCES swapmgrdirectories (dirid))",
			"CREATE INDEX swapmgrdirdirectoryidx " +
				"ON swapmgrdirectories (directory)",
			"CREATE INDEX swapmgrdirlastidlestartidx " +
				"ON swapmgrdirectories (lastidlestart)",
			"CREATE INDEX swapmgrdirresdirididx " +
				"ON swapmgrdirectoryreservations (dirid)",
			"CREATE INDEX swapmgrdirrestimeacquiredidx " +
				"ON swapmgrdirectoryreservations (timeacquired)");
	}
	
	public void cleanupExpiredReservations(
		Connection conn, long expiryTimeMillis) throws SQLException
	{
		Statement stmt = null;
		
		try
		{
			stmt = conn.createStatement();
			
			stmt.executeUpdate(String.format(
				"DELETE FROM swapmgrdirectoryreservations " +
				"WHERE " +
					"{fn TIMESTAMPDIFF(SQL_TSI_SECOND, " +
						"timeacquired, CURRENT_TIMESTAMP)} > %d", 
				(expiryTimeMillis / 1000L)));
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	public void patchIdles(Connection conn) throws SQLException
	{
		Statement stmt = null;
		
		try
		{
			stmt = conn.createStatement();
			
			stmt.executeUpdate(
				"UPDATE swapmgrdirectories " +
					"SET lastidlestart = CURRENT_TIMESTAMP " +
				"WHERE dirid NOT IN " +
					"(SELECT dirid FROM swapmgrdirectoryreservations)");
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	public Collection<File> cleanupExpiredDirectories(Connection conn,
		long idleTimeoutMillis) throws SQLException
	{
		Collection<File> ret = new LinkedList<File>();
		Statement stmt = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			pStmt = conn.prepareStatement(
				"DELETE FROM swapmgrdirectories WHERE dirid = ?");
			
			rs = stmt.executeQuery(String.format(
				"SELECT dirid, directory FROM swapmgrdirectories " +
				"WHERE " +
					"{fn TIMESTAMPDIFF(SQL_TSI_SECOND, " +
						"lastidlestart, CURRENT_TIMESTAMP)} > %d", 
				(idleTimeoutMillis / 1000L)));
			while (rs.next())
			{
				pStmt.setLong(1, rs.getLong(1));
				pStmt.addBatch();
				
				ret.add(new File(rs.getString(2)));
			}
			
			pStmt.executeBatch();
			
			return ret;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	public long reserveDirectory(Connection conn, File directory)
		throws SQLException
	{
		Statement sstmt = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		long dirid;
		
		try
		{
			stmt = conn.prepareStatement(
				"SELECT dirid FROM swapmgrdirectories WHERE directory = ?");
			stmt.setString(1, directory.toString());
			rs = stmt.executeQuery();
			
			if (!rs.next())
				dirid = createNewDirectoryEntry(conn, directory);
			else
			{
				dirid = rs.getLong(1);
				
				updateIdleStampToNull(conn, dirid);
			}
			
			rs.close();
			rs = null;
			
			stmt.close();
			stmt = null;
			
			stmt = conn.prepareStatement(
				"INSERT INTO swapmgrdirectoryreservations " +
					"(dirid, timeacquired) VALUES (?, CURRENT_TIMESTAMP)");
			stmt.setLong(1, dirid);
			if (stmt.executeUpdate() != 1)
				throw new SQLException(
					"Unable to insert new reservation into database.");
			
			sstmt = conn.createStatement();
			rs = sstmt.executeQuery("VALUES IDENTITY_VAL_LOCAL()");
			if (!rs.next())
				throw new SQLException(
					"Unable to get last inserted primary key.");
			
			return rs.getLong(1);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			StreamUtils.close(sstmt);
		}
	}
	
	private long createNewDirectoryEntry(Connection conn, File directory)
		throws SQLException
	{
		Statement stmt = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		
		try
		{
			pStmt = conn.prepareStatement(
				"INSERT INTO swapmgrdirectories " +
					"(directory, lastidlestart) VALUES (?, NULL)");
			pStmt.setString(1, directory.toString());
			if (pStmt.executeUpdate() != 1)
				throw new SQLException(
					"Unable to insert new directory entry.");
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("VALUES IDENTITY_VAL_LOCAL()");
			if (!rs.next())
				throw new SQLException(
					"Unable to get last created primary key.");
			
			return rs.getLong(1);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			StreamUtils.close(pStmt);
		}
	}
	
	private void updateIdleStampToNull(Connection conn, long dirid)
		throws SQLException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = conn.prepareStatement(
				"UPDATE swapmgrdirectories SET lastidlestart = NULL " +
				"WHERE dirid = ?");
			stmt.setLong(1, dirid);
			stmt.executeUpdate();
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	public long releaseReservation(Connection conn, long reservationID)
		throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		long dirid;
		
		try
		{
			stmt = conn.prepareStatement(
				"SELECT dirid FROM swapmgrdirectoryreservations " +
				"WHERE reservationid = ?");
			stmt.setLong(1, reservationID);
			rs = stmt.executeQuery();
			
			if (!rs.next())
				throw new SQLException(String.format(
					"Unable to locate reservation %d in database.", 
					reservationID));
			dirid = rs.getLong(1);
			
			stmt.close();
			stmt = null;
			
			stmt = conn.prepareStatement(
				"DELETE FROM swapmgrdirectoryreservations " +
				"WHERE reservationid = ?");
			stmt.setLong(1, reservationID);
			stmt.executeUpdate();
			
			return dirid;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);		
		}
	}
	
	public void patchIdle(Connection conn, long dirid) throws SQLException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = conn.prepareStatement(
				"UPDATE swapmgrdirectories " +
					"SET lastidlestart = CURRENT_TIMESTAMP " +
				"WHERE dirid = ? AND " +
					"dirid NOT IN " +
						"(SELECT dirid FROM swapmgrdirectoryreservations)");
			stmt.setLong(1, dirid);
			
			stmt.executeUpdate();
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
}