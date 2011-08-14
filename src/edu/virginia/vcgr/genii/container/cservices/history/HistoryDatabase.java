package edu.virginia.vcgr.genii.container.cservices.history;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.client.history.HistoryEventData;
import edu.virginia.vcgr.genii.client.history.HistoryEventSource;
import edu.virginia.vcgr.genii.client.history.SequenceNumber;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;

public class HistoryDatabase
{
	static private Log _logger = LogFactory.getLog(HistoryDatabase.class);
	
	static final private String []CREATE_TABLE_STMTS = {
		"CREATE TABLE historyrecords(" +
			"hrid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
			"resourceid VARCHAR(128) NOT NULL," +
			"sequencenumber VARCHAR(32) NOT NULL," +
			"level VARCHAR(16) NOT NULL," +
			"category VARCHAR(64) NOT NULL," +
			"createtimestamp TIMESTAMP NOT NULL WITH DEFAULT CURRENT TIMESTAMP," +
			"properties BLOB(2G) NOT NULL," +
			"eventsource BLOB(2G) NOT NULL," +
			"eventdata BLOB(2G) NOT NULL," +
			"expirationtime TIMESTAMP," +
			"CONSTRAINT sequniqconstraint UNIQUE(resourceid, sequencenumber))",
		"CREATE INDEX historyrecordsresourceididx ON historyrecords(resourceid)",
		"CREATE INDEX historyrecordsexpirationtimeidx ON historyrecords(expirationtime)",
		"CREATE TABLE historystale ( resourceID VARCHAR(128) NOT NULL PRIMARY KEY )"
	};
	
	static private class CloseableIteratorImpl
		implements CloseableIterator<HistoryEvent>
	{
		private LinkedList<HistoryEvent> _stagingArea = 
			new LinkedList<HistoryEvent>();
		private DatabaseConnectionPool _connectionPool;
		private Connection _connection;
		private Statement _stmt;
		private ResultSet _rs;
		
		private CloseableIteratorImpl(DatabaseConnectionPool connectionPool,
			Connection connection, Statement stmt, ResultSet rs)
		{
			_connectionPool = connectionPool;
			_connection = connection;
			_stmt = stmt;
			_rs = rs;
		}
		
		@Override
		protected void finalize() throws Throwable
		{
			close();
		}
		
		@Override
		synchronized final public void close() throws IOException
		{
			if (_connection != null)
			{
				StreamUtils.close(_rs);
				StreamUtils.close(_stmt);
				_connectionPool.release(_connection);
				_connection = null;
			}
		}

		@Override
		final public boolean hasNext()
		{
			if (!_stagingArea.isEmpty())
				return true;
			
			try
			{
				if (!_rs.next())
					return false;
				_stagingArea.addLast(historyEventFromStandardResultSet(_rs));
				return true;
			}
			catch (SQLException e)
			{
				throw new RuntimeException(
					"Unable to advance result set.", e);
			}
		}

		@Override
		final public HistoryEvent next()
		{
			if (_stagingArea.isEmpty())
				hasNext();
			return _stagingArea.removeFirst();
		}

		@Override
		final public void remove()
		{
			throw new UnsupportedOperationException(
				"Not allowed to remove items from this kind of iterator.");
		}
	}
	
	@SuppressWarnings("unchecked")
	static private HistoryEvent historyEventFromStandardResultSet(ResultSet rs)
		throws SQLException
	{
		return new HistoryEvent(
			SequenceNumber.valueOf(rs.getString(1)),
			convert(rs.getTimestamp(4)),
			(HistoryEventSource)DBSerializer.fromBlob(rs.getBlob(6)),
			HistoryEventLevel.valueOf(rs.getString(2)),
			HistoryEventCategory.valueOf(rs.getString(3)),
			(Map<String, String>)DBSerializer.fromBlob(rs.getBlob(5)),
			(HistoryEventData)DBSerializer.fromBlob(rs.getBlob(7)));
	}
	
	static private Calendar convert(Timestamp stamp)
	{
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(stamp.getTime());
		return ret;
	}
	
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
			_logger.warn("Error trying to create accounting record tables.",
				sqe);
		}
	}
	
	static long addRecord(Connection connection,
		String resourceID, SequenceNumber number,
		Calendar createTimestamp,
		HistoryEventCategory category, 
		HistoryEventLevel level, Map<String, String> properties,
		HistoryEventSource eventSource, HistoryEventData eventData,
		Calendar expirationTime)
			throws SQLException
	{
		if (number == null)
			throw new IllegalArgumentException(
				"Sequence number cannot be null.");
		
		if (category == null)
			category = HistoryEventCategory.Default;
		
		if (createTimestamp == null)
			createTimestamp = Calendar.getInstance();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"INSERT INTO historyrecords" +
					"(resourceid, sequencenumber, level, category, " +
					"properties, eventsource, eventdata, expirationtime," +
					" createtimestamp) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
			
			stmt.setString(1, resourceID);
			stmt.setString(2, number.toString());
			stmt.setString(3, level.name());
			stmt.setString(4, category.name());
			stmt.setBlob(5, DBSerializer.toBlob(
				properties, "historyrecords", "properties"));
			stmt.setBlob(6, DBSerializer.toBlob(
				eventSource, "historyrecords", "eventsource"));
			stmt.setBlob(7, DBSerializer.toBlob(
				eventData, "historyrecords", "eventdata"));
			
			if (expirationTime == null)
				stmt.setNull(8, Types.TIMESTAMP);
			else
				stmt.setTimestamp(8,
					new Timestamp(expirationTime.getTimeInMillis()));
			
			stmt.setTimestamp(9,
				new Timestamp(createTimestamp.getTimeInMillis()));
			
			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			
			if (!rs.next())
				throw new SQLException(
					"Unable to get generated key from history record insertion.");
			
			return rs.getLong(1);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	static SequenceNumber getNextLargestLevel1SequenceNumber(Connection connection,
		String resourceID) throws SQLException
	{
		SequenceNumber largest = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT sequencenumber FROM historyrecords WHERE resourceid = ?");
			stmt.setString(1, resourceID);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				SequenceNumber next = SequenceNumber.valueOf(rs.getString(1));
				if (largest == null || next.compareTo(largest) > 0)
					largest = next;
			}
			
			if (largest == null)
				largest = new SequenceNumber();
			else
				largest = largest.next();
			
			return largest;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	static HistoryEvent getEvent(Connection connection,
		long hrid) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT sequencenumber, level, category, " +
					"createtimestamp, properties, eventsource, " +
					"eventdata FROM historyrecords " +
				"WHERE hrid = ?");
			stmt.setLong(1, hrid);
			rs = stmt.executeQuery();
			
			if (!rs.next())
				return null;
			
			return historyEventFromStandardResultSet(rs);
		}		
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	static Collection<HistoryEvent> getEvents(Connection connection,
		String resourceID) throws SQLException
	{
		Collection<HistoryEvent> ret = new LinkedList<HistoryEvent>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT sequencenumber, level, category, " +
					"createtimestamp, properties, eventsource, " +
					"eventdata FROM historyrecords " +
				"WHERE resourceid = ?");
			stmt.setString(1, resourceID);
			rs = stmt.executeQuery();
			
			while (rs.next())
				ret.add(historyEventFromStandardResultSet(rs));
			
			return ret;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	static CloseableIterator<HistoryEvent> iterateEvents(
		DatabaseConnectionPool connectionPool,
		Connection connection, String resourceID) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT sequencenumber, level, category, " +
					"createtimestamp, properties, eventsource, " +
					"eventdata FROM historyrecords " +
				"WHERE resourceid = ?");
			stmt.setString(1, resourceID);
			rs = stmt.executeQuery();
			
			CloseableIterator<HistoryEvent> ret = new CloseableIteratorImpl(
				connectionPool, connection, stmt, rs);
			
			rs = null;
			stmt = null;
			
			return ret;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	static void cleanupDeadEvents(Connection connection) throws SQLException
	{
		Statement stmt = null;
		
		try
		{
			stmt = connection.createStatement();
			stmt.executeUpdate(
				"DELETE FROM historyrecords WHERE " +
					"(expirationtime IS NOT NULL) AND " +
					"(expirationtime < CURRENT TIMESTAMP)");
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	static Collection<HistoryEvent> getEvents(Connection connection) 
		throws SQLException
	{
		Collection<HistoryEvent> ret = new LinkedList<HistoryEvent>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT sequencenumber, level, category, " +
					"createtimestamp, properties, eventsource, " +
					"eventdata FROM historyrecords");
			rs = stmt.executeQuery();
			
			while (rs.next())
				ret.add(historyEventFromStandardResultSet(rs));
			
			return ret;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
		
	static void deleteRecords(Connection connection,
		String resourceID) throws SQLException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"DELETE FROM historyrecords WHERE resourceID = ?");
			stmt.setString(1, resourceID);
			stmt.executeUpdate();
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	static void deleteRecordsLike(Connection connection,
		String likeConstant) throws SQLException
	{
		Statement stmt = null;
		
		try
		{
			stmt = connection.createStatement();
			stmt.executeUpdate(String.format(
				"DELETE FROM historyrecords WHERE resourceID LIKE '%s'",
				likeConstant));
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	static void loadStaleHistory(Connection connection, Collection<String> buffer) throws SQLException
	{
		Statement stmt = null;	
		ResultSet rs = null;
		
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery("select resourceid from historystale");
			
			while (rs.next())
				buffer.add(rs.getString(1));						
			
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	static void addStaleRecord(String resourceID, Connection conn) throws SQLException
	{
		PreparedStatement st=null;
		try
		{
			st = conn.prepareStatement(
					"INSERT INTO historystale " +
						"(resourceid) " +
					"VALUES(?)");
				
			st.setString(1, resourceID);
			st.executeUpdate();
			
		}
		finally
		{
			StreamUtils.close(st);
		}

	}
	
	static void removeStaleRecord(String resourceID, Connection conn) throws SQLException
	{
		PreparedStatement st=null;
		try
		{
			st = conn.prepareStatement(
					"DELETE from historystale WHERE resourceID = ?");
			st.setString(1, resourceID);
			st.executeUpdate();			
		}
		finally
		{
			StreamUtils.close(st);
		}

	}
	
}