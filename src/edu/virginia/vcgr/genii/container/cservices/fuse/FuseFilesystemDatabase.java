package edu.virginia.vcgr.genii.container.cservices.fuse;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;

class FuseFilesystemDatabase
{
	static private Log _logger = LogFactory.getLog(
		FuseFilesystemDatabase.class);
	
	/* 45 days */
	static final private long DEFAULT_TTL = 1000L * 60 * 60 * 24 * 45;
	
	static final private String []CREATE_TABLE_STMTS = {
		"CREATE TABLE fusefilesystems(" +
			"id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
			"parentdir VARCHAR(512) NOT NULL," +
			"mountpoint VARCHAR(128) NOT NULL," +
			"deathtime TIMESTAMP," +
			"createtime TIMESTAMP NOT NULL WITH DEFAULT CURRENT_TIMESTAMP)",
	};
	
	static void createTables(Connection connection)
	{
		try
		{
			for (String createStmt : CREATE_TABLE_STMTS)
				DatabaseTableUtils.createTables(
					connection, false, createStmt);
		}
		catch (SQLException sqe)
		{
			_logger.warn("Error trying to create fuse filesystem tables.",
				sqe);
		}
	}
	
	static void loadAll(Connection connection,
		Map<File, Map<String, Long>> parentDir2MountPoint2Id)
			throws SQLException
	{
		Set<Long> deleteSet = new HashSet<Long>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT id, parentdir, mountpoint, deathtime FROM fusefilesystems");
			rs = stmt.executeQuery();
			while (rs.next())
			{
				long id = rs.getLong(1);
				String parentDirString = rs.getString(2);
				String mountPoint = rs.getString(3);
				Timestamp deathTime = rs.getTimestamp(4);
				File parentDir = new File(parentDirString);
				
				if (!parentDir.exists() || !parentDir.isDirectory())
					deleteSet.add(id);
				else
				{
					File mountPointDir = new File(parentDir, mountPoint);
					if (!mountPointDir.exists() || !mountPointDir.isDirectory())
						deleteSet.add(id);
					else if (deathTime != null && deathTime.getTime() < System.currentTimeMillis())
					{
						deleteSet.add(id);
						mountPointDir.delete();
					} else
					{
						Map<String, Long> mountPoint2Id = 
							parentDir2MountPoint2Id.get(parentDir);
						if (mountPoint2Id == null)
							parentDir2MountPoint2Id.put(parentDir,
								mountPoint2Id = new HashMap<String, Long>());
						mountPoint2Id.put(mountPoint, id);
					}
				}
			}
			
			if (!deleteSet.isEmpty())
			{
				stmt.close();
				stmt = null;
				stmt = connection.prepareStatement(
					"DELETE FROM fusefilesystems WHERE id = ?");
				for (Long id : deleteSet)
				{
					stmt.setLong(1, id);
					stmt.addBatch();
				}
				
				stmt.executeBatch();
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	static long store(Connection conn, File parent, String mount) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.prepareStatement(
				"INSERT INTO fusefilesystems(" +
					"parentdir, mountpoint, deathtime) VALUES(?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, parent.getAbsolutePath());
			stmt.setString(2, mount);
			Timestamp deathTime = new Timestamp(
				System.currentTimeMillis() + DEFAULT_TTL);
			stmt.setTimestamp(3, deathTime);
			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (!rs.next())
				throw new SQLException(
					"Unable to retrieve auto-generated keys!");
			return rs.getLong(1);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	static void remove(Connection conn, long id) throws SQLException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = conn.prepareStatement(
				"DELETE FROM fusefilesystems WHERE id = ?");
			stmt.setLong(1, id);
			stmt.executeUpdate();
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
}