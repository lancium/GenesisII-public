package edu.virginia.vcgr.genii.container.alarms;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.axis.AxisFault;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;

public class AlarmManager
{
	static private Log _logger = LogFactory.getLog(AlarmManager.class);
	
	static private AlarmManager _manager = null;
	
	synchronized static public void initializeAlarmManager()
		throws SQLException
	{
		if (_manager == null)
			_manager = new AlarmManager();
	}
	
	synchronized static public AlarmManager getManager()
	{
		if (_manager == null)
			throw new RuntimeException(
				"Alarm manager has not been initialized yet.");
		
		return _manager;
	}
	
	private DatabaseConnectionPool _connectionPool = null;
	
	private AlarmTable _alarms = new AlarmTable();
	
	@SuppressWarnings("unchecked")
	private Class<? extends GenesisIIBase> 
		findServiceImplClass(EndpointReferenceType target)
			throws AxisFault
	{
		JavaServiceDesc desc = Container.findService(target);
		return (Class<? extends GenesisIIBase>)desc.getImplClass();
	}
	
	private boolean createTableIfNecessary(Connection conn)
	{
		try
		{
			DatabaseTableUtils.createTables(conn, false, 
				"CREATE TABLE alarmtable (" +
					"alarmid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
					"nextoccurance TIMESTAMP NOT NULL," +
					"repeatinterval BIGINT," +
					"callingcontext BLOB(128K)," +
					"target BLOB(128K)," +
					"methodname VARCHAR(256) NOT NULL," +
					"userdata BLOB(128K))");
				conn.commit();
			return true;
		}
		catch (SQLException sqe)
		{
			_logger.error("Unable to create alarm table.", sqe);
		}
		
		return false;
	}
	
	private void reloadFromDatabase(Connection conn)
		throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(
				"SELECT alarmid, nextoccurance FROM alarmtable");
			while (rs.next())
			{
				_alarms.put(
					rs.getLong(1),
					rs.getTimestamp(2));
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	private void initializeDatabase() throws SQLException
	{
		Connection conn = null;
		
		try
		{
			conn = _connectionPool.acquire();
			if (!createTableIfNecessary(conn))
				reloadFromDatabase(conn);
		}
		finally
		{
			_connectionPool.release(conn);
		}
	}
	
	private AlarmManager() throws SQLException
	{
		_connectionPool = 
			(DatabaseConnectionPool)NamedInstances.getServerInstances(
				).lookup("connection-pool");
		if (_connectionPool == null)
			throw new RuntimeException(
				"Unable to find named instance \"connection-pool\".");
		
		initializeDatabase();
		Thread th = new Thread(new AlarmRunner());
		th.setDaemon(true);
		th.setName("Alarm Manager Thread");
		th.start();
	}
	
	public void cancelAlarm(long alarmKey)
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		
		synchronized(_alarms)
		{
			_alarms.remove(alarmKey);
		}
		
		try
		{
			conn = _connectionPool.acquire();
			stmt = conn.prepareStatement(
				"DELETE FROM alarmtable WHERE alarmid = ?");
			stmt.setLong(1, alarmKey);
			stmt.executeUpdate();
			conn.commit();
		}
		catch (SQLException sqe)
		{
			throw new RuntimeException("Unable to cancel alarm.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(conn);
		}
	}
	
	/**
	 * Add a new alarm to the system.
	 * 
	 * @param nextOccurance The date on which the next occurance of the 
	 * alarm should take place.  This value MUST be non-null, but MAY be
	 * in the past (it will get executed eventually).
	 * @param repeatIntervalMS This is the number of milliseconds between
	 * alarms.  This value can be 0 or negative indicating that the alarm
	 * does NOT repeat.
	 * @param callingContext The calling context to use when calling the
	 * alarm.  This value CAN be null in which case the current calling context
	 * will be used.
	 * @param targetEPR The target EPR for the service to receive the alarm.
	 * This can be null in which case the current EPR will be used.
	 * @param methodName The name of the method on the service to handle
	 * the alarm.  This cannot be null.  This method MUST have a signature
	 * of "[public|protected] void methodName(
	 * 		AlarmIdentifier id, Type userData)" where
	 * Type is a compatible type to userData's type.
	 * @param userData Any user data (or null) to pass along for alarms.
	 * @return An alarm identifier.
	 */
	public AlarmIdentifier addAlarm(Date nextOccurance,
		long repeatIntervalMS, ICallingContext callingContext,
		EndpointReferenceType targetEPR, String methodName,
		Serializable userData)
	{
		Connection conn = null;
		PreparedStatement addStmt = null;
		Statement idStmt = null;
		ResultSet rs = null;
		
		if (callingContext == null)
		{
			try
			{
				callingContext = ContextManager.getCurrentContext(false);
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to get current calling context.", cause);
			}
		}
		
		if (targetEPR == null)
		{
			try
			{
				targetEPR = 
					(EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
						WorkingContext.EPR_PROPERTY_NAME);
			}
			catch (Throwable cause)
			{
				_logger.error("Unable to get current target EPR.", cause);
			}
		}
		
		try
		{
			conn = _connectionPool.acquire();
			addStmt = conn.prepareStatement(
				"INSERT INTO alarmtable(" +
					"nextoccurance, repeatinterval, callingcontext, " +
					"target, methodname, userdata) " +
				"VALUES (?, ?, ?, ?, ?, ?)");
			addStmt.setTimestamp(1, new Timestamp(nextOccurance.getTime()));
			addStmt.setLong(2, repeatIntervalMS);
			addStmt.setBlob(3, DBSerializer.toBlob(callingContext));
			addStmt.setBlob(4, EPRUtils.toBlob(targetEPR));
			addStmt.setString(5, methodName);
			addStmt.setBlob(6, DBSerializer.toBlob(userData));
			if (addStmt.executeUpdate() != 1)
				throw new RuntimeException("Unable to add alarm to database.");
			
			idStmt = conn.createStatement();
			rs = idStmt.executeQuery("values IDENTITY_VAL_LOCAL()");
			if (!rs.next())
				throw new RuntimeException("Unable to add alarm to database.");
			
			long alarmid = rs.getLong(1);
			synchronized(_alarms)
			{
				_alarms.put(alarmid, nextOccurance);
				_alarms.notify();
			}
			conn.commit();
			
			return new AlarmIdentifier(alarmid);
		}
		catch (SQLException sqe)
		{
			try { conn.rollback(); } catch (Throwable cause) {}
			throw new RuntimeException("Unable to add alarm.", sqe);
		}
		catch (ResourceException re)
		{
			try { conn.rollback(); } catch (Throwable cause) {}
			throw new RuntimeException("Unable to add alarm.", re);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(idStmt);
			StreamUtils.close(addStmt);
			_connectionPool.release(conn);
		}
	}
	
	private void callMethod(Class<? extends GenesisIIBase> cl,
		long alarmid, String methodName, Object userData)
			throws ResourceUnknownFaultType
	{
		try
		{
			Constructor<? extends GenesisIIBase> cons =
				cl.getConstructor();
			GenesisIIBase inst = cons.newInstance();
			inst.callAlarmMethod(new AlarmIdentifier(alarmid),
				methodName, userData);
		}
		catch (ResourceUnknownFaultType ruft)
		{
			throw ruft;
		}
		catch (Throwable cause)
		{
			_logger.error("Unable to handle alarm.", cause);
		}
	}
	
	private void updateDatabase(
		PreparedStatement removeStmt, PreparedStatement updateStmt,
		long alarmid, long repeatInterval, Date now)
		throws Throwable
	{
		Timestamp nextOccurance = null;
		
		try
		{
			if (repeatInterval <= 0)
			{
				removeStmt.setLong(1, alarmid);
				removeStmt.executeUpdate();
			} else
			{
				nextOccurance = new Timestamp(
					now.getTime() + repeatInterval);
				updateStmt.setTimestamp(1, nextOccurance);
				updateStmt.setLong(2, alarmid);
				updateStmt.executeUpdate();
			}
		}
		finally
		{
			if (nextOccurance != null)
			{
				synchronized(_alarms)
				{
					_alarms.put(alarmid, nextOccurance);
				}
			}
		}
	}
	
	private void performAlarm(Connection conn,
		PreparedStatement removeStmt, PreparedStatement updateStmt,
		long alarmid, long repeatInterval,
		ICallingContext callingContext,
		EndpointReferenceType target,
		String methodName, Object userData)
	{
		boolean needRelease = false;
		WorkingContext newContext = null;
		
		try
		{
			if (target == null)
			{
				removeStmt.setLong(1, alarmid);
				removeStmt.executeUpdate();
				conn.commit();
				return;
			}
			
			WorkingContext.temporarilyAssumeNewIdentity(target);
			needRelease = true;
			
			WorkingContext.getCurrentWorkingContext().setProperty(
				WorkingContext.CURRENT_CONTEXT_KEY, callingContext);
			
			callMethod(findServiceImplClass(target), 
				alarmid, methodName, userData);
		}
		catch (ResourceUnknownFaultType ruft)
		{
			_logger.warn("Resource was unknown for alarm...removing it.");
			repeatInterval = -1L;
		}
		catch (Throwable cause)
		{
			_logger.error("Problem handling alarm.", cause);
		}
		finally
		{
			try
			{
				if (needRelease)
					WorkingContext.releaseAssumedIdentity();
			
				if (newContext != null)
					WorkingContext.setCurrentWorkingContext(null);
				
				updateDatabase(removeStmt, updateStmt,
					alarmid, repeatInterval, new Date());
				conn.commit();
			}
			catch (Throwable cause)
			{
				try { conn.rollback(); } catch (Throwable cause2) {}
				_logger.error("Unable to release assumed identity.", cause);
			}
		}
	}
		
	private void performAlarms(Collection<AlarmDescriptor> dueAlarms)
	{
		Connection conn = null;
		PreparedStatement getInfoStmt = null;
		PreparedStatement removeStmt = null;
		PreparedStatement updateStmt = null;
		ResultSet rs = null;
		
		try
		{
			conn = _connectionPool.acquire();
			getInfoStmt = conn.prepareStatement(
				"SELECT repeatinterval, callingcontext, target, " +
					"methodname, userdata " +
				"FROM alarmtable WHERE alarmid = ?");
			removeStmt = conn.prepareStatement(
				"DELETE FROM alarmtable WHERE alarmid = ?");
			updateStmt = conn.prepareStatement(
				"UPDATE alarmtable SET nextoccurance = ? WHERE alarmid = ?");
		
			for (AlarmDescriptor desc : dueAlarms)
			{
				try
				{
					getInfoStmt.setLong(1, desc.getAlarmID());
					rs = getInfoStmt.executeQuery();
					if (rs.next())
					{
						performAlarm(conn, removeStmt, updateStmt,
							desc.getAlarmID(), rs.getLong(1),
							(ICallingContext)DBSerializer.fromBlob(rs.getBlob(2)),
							(EndpointReferenceType)EPRUtils.fromBlob(rs.getBlob(3)),
							rs.getString(4), DBSerializer.fromBlob(rs.getBlob(5)));
					}
				}
				catch (Throwable cause)
				{
					_logger.error("Problem handling alarms.", cause);
				}
			}
		}
		catch (Throwable cause)
		{
			_logger.error("Problem handling alarms.", cause);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(getInfoStmt);
			StreamUtils.close(removeStmt);
			StreamUtils.close(updateStmt);
			_connectionPool.release(conn);
		}
	}
	
	private void handleAllDueAlarms(Date now)
	{
		Collection<AlarmDescriptor> passedAlarms = 
			new LinkedList<AlarmDescriptor>();
		
		synchronized(_alarms)
		{
			Iterator<AlarmDescriptor> iter = _alarms.iterator();
			while (iter.hasNext())
			{
				AlarmDescriptor desc = iter.next();
				if (desc.getNextOccurance().compareTo(now) <= 0)
				{
					passedAlarms.add(desc);
					iter.remove();
				} else
					break;
			}
		}
		
		performAlarms(passedAlarms);
	}
	
	private class AlarmRunner implements Runnable
	{
		@Override
		public void run()
		{
			long sleepTime;
			
			while (true)
			{
				Date now = new Date();
				handleAllDueAlarms(now);
				now = new Date();
				
				synchronized(_alarms)
				{
					AlarmDescriptor next = _alarms.peek();
					if (next != null)
					{
						sleepTime = 
							next.getNextOccurance().getTime() - now.getTime();
						if (sleepTime <= 0)
							sleepTime = 1L;
					} else
					{
						sleepTime = 0;
					}
					
					try
					{
						_alarms.wait(sleepTime);
					}
					catch (InterruptedException ie)
					{
						Thread.interrupted();
					}
				}
			}
		}
	}
}