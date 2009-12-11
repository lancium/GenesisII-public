package edu.virginia.vcgr.genii.container.cservices.percall;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Triple;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;

class PersistentOutcallDatabase
{
	static private Log _logger = LogFactory.getLog(PersistentOutcallDatabase.class);
	
	static final private String CREATE_TABLE_STMT =
		"CREATE TABLE persistentoutcalls(" +
			"id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
			"target BLOB(2G) NOT NULL," +
			"outcallhandler BLOB(2G) NOT NULL," +
			"callingcontext BLOB(2G) NOT NULL," +
			"nextattempt TIMESTAMP NOT NULL," +
			"createtime TIMESTAMP NOT NULL," +
			"attemptscheduler BLOB(2G) NOT NULL," +
			"numattempts INTEGER NOT NULL)";
	
	static private Calendar convert(Timestamp ts)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(ts);
		return c;
	}
	
	static private Timestamp convert(Calendar c)
	{
		Timestamp ts = new Timestamp(c.getTimeInMillis());
		return ts;
	}
	
	static void createTables(Connection connection)
	{
		try
		{
			DatabaseTableUtils.createTables(
				connection, false, CREATE_TABLE_STMT);
		}
		catch (SQLException sqe)
		{
			_logger.warn("Error trying to create persistent outcall table.",
				sqe);
		}
	}
	
	static NavigableSet<PersistentOutcallEntry> readTable(Connection connection)
		throws SQLException
	{
		TreeSet<PersistentOutcallEntry> ret = new TreeSet<PersistentOutcallEntry>(
			PersistentOutcallEntry.NEXT_ATTEMPT_COMPARATOR);
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery(
				"SELECT id, nextattempt, createtime," +
				"attemptscheduler, numattempts FROM persistentoutcalls");
			while (rs.next())
			{
				ret.add(new PersistentOutcallEntry(
					rs.getLong(1), rs.getInt(5),
					convert(rs.getTimestamp(2)),
					convert(rs.getTimestamp(3)),
					(AttemptScheduler)DBSerializer.fromBlob(rs.getBlob(4))));
					
			}
			
			return ret;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	static PersistentOutcallEntry add(Connection connection,
		EndpointReferenceType target, ICallingContext callingContext,
		OutcallActor outcallActor, AttemptScheduler scheduler) 
			throws SQLException
	{
		PreparedStatement stmt = null;
		Statement sstmt = null;
		ResultSet rs = null;
		
		Calendar createTime = Calendar.getInstance();
		Calendar nextAttempt = createTime;
		int numAttempts = 0;
		
		try
		{
			if (callingContext == null)
				callingContext = ContextManager.getCurrentContext();
		}
		catch (IOException ioe)
		{
			throw new SQLException(
				"Unable to get current working context.", ioe);
		}
		
		try
		{
			stmt = connection.prepareStatement(
				"INSERT INTO persistentoutcalls(target, outcallhandler, " +
				"callingcontext, nextattempt, createtime, attemptscheduler, " +
				"numattempts) VALUES(?, ?, ?, ?, ?, ?, ?)");
			stmt.setBlob(1, EPRUtils.toBlob(
				target, "persistentoutcalls", "target"));
			stmt.setBlob(2, DBSerializer.toBlob(
				outcallActor, "persistentoutcalls", "outcallhandler"));
			stmt.setBlob(3, DBSerializer.toBlob(
				callingContext, "persistentoutcalls", "callingcontext"));
			stmt.setTimestamp(4, convert(nextAttempt));
			stmt.setTimestamp(5, convert(createTime));
			stmt.setBlob(6, DBSerializer.toBlob(
				scheduler, "persistentoutcalls", "attemptscheduler"));
			stmt.setInt(7, numAttempts);
			if (stmt.executeUpdate() != 1)
				throw new SQLException(
					"Unable to add persisent outcall entry to db.");
			
			sstmt = connection.createStatement();
			rs = sstmt.executeQuery("VALUES IDENTITY_VAL_LOCAL()");
			if (!rs.next())
				throw new SQLException("Unable to get last added id from db.");
			return new PersistentOutcallEntry(rs.getLong(1), numAttempts,
				nextAttempt, createTime, scheduler);
		}
		catch (ResourceException e)
		{
			throw new SQLException("Unable to serialize EPR to a blob.", e);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			StreamUtils.close(sstmt);
		}
	}

	static Triple<EndpointReferenceType, ICallingContext, OutcallActor> 
		getCommunicationInformation(Connection connection, 
			PersistentOutcallEntry entry) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT target, callingcontext, outcallhandler " +
				"FROM persistentoutcalls WHERE id = ?");
			stmt.setLong(1, entry.entryID());
			rs = stmt.executeQuery();
			if (rs.next())
				return new Triple<EndpointReferenceType, ICallingContext, OutcallActor>(
					EPRUtils.fromBlob(rs.getBlob(1)),
					(ICallingContext)DBSerializer.fromBlob(rs.getBlob(2)),
					(OutcallActor)DBSerializer.fromBlob(rs.getBlob(3)));
			else
				throw new SQLException(String.format(
					"Unable to find persistent outcall entry %d",
					entry.entryID()));
		}
		catch (ResourceException re)
		{
			throw new SQLException(String.format(
				"Error deserializing EPR from database for persisent outcall entry %d.",
				entry.entryID()), re);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	static void update(Connection connection, 
		PersistentOutcallEntry entry)
			throws SQLException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"UPDATE persistentoutcalls " +
					"SET nextattempt = ?, numattempts = ? WHERE id = ?");
			stmt.setTimestamp(1, convert(entry.nextAttempt()));
			stmt.setInt(2, entry.numAttempts());
			stmt.setLong(3, entry.entryID());
			if (stmt.executeUpdate() != 1)
				throw new SQLException(
					"Unable to update persistent outcall entry.");
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	static void remove(Connection connection,
		PersistentOutcallEntry entry) throws SQLException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"DELETE FROM persistentoutcalls WHERE id = ?");
			stmt.setLong(1, entry.entryID());
			stmt.executeUpdate();
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
}