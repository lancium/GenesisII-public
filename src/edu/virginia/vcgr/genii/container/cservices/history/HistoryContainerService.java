package edu.virginia.vcgr.genii.container.cservices.history;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.alarms.AlarmHandler;
import edu.virginia.vcgr.genii.client.alarms.AlarmToken;
import edu.virginia.vcgr.genii.client.alarms.InMemoryAlarmManager;
import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.client.history.HistoryEventData;
import edu.virginia.vcgr.genii.client.history.HistoryEventSource;
import edu.virginia.vcgr.genii.client.history.SequenceNumber;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;

public class HistoryContainerService extends AbstractContainerService
{
	static private Log _logger = LogFactory.getLog(HistoryContainerService.class);

	static final private long CLEANUP_INTERVAL = 1000L; // 1000L * 60 * 60; // 1 hour.

	static final public String SERVICE_NAME = "History Service";

	static final private Queue<String> queue = new LinkedList<String>();

	static final private Object lock = new Object();

	private class CleanupAlarmHandler implements AlarmHandler
	{
		@Override
		public void alarmWentOff(AlarmToken token, Object userData)
		{
			Connection connection = null;

			try {
				connection = getConnectionPool().acquire(false);
				HistoryDatabase.cleanupDeadEvents(connection);
				String resourceId = new String();

				synchronized (lock) {
					resourceId = queue.poll();
				}

				if (resourceId != null) {
					deleteRecords(connection, resourceId);
					HistoryDatabase.removeStaleRecord(resourceId, connection);
					_logger.info(String.format("Deleting history for resource %s", resourceId));
				}

				connection.commit(); // commits combined to 1 for performance

			} catch (SQLException sqe) {
				_logger.warn("Error trying to clean up dead history events.", sqe);
			} finally {
				getConnectionPool().release(connection);
			}
		}
	}

	static private class HistoryEventTokenImpl implements HistoryEventToken
	{
		static final long serialVersionUID = 0L;

		private long _historyRecordID;

		private HistoryEventTokenImpl(long historyRecordID)
		{
			_historyRecordID = historyRecordID;
		}

		@Override
		final public SequenceNumber retrieve() throws SQLException
		{
			HistoryContainerService service = ContainerServices.findService(HistoryContainerService.class);
			HistoryEvent event = service.getEvent(_historyRecordID);
			if (event != null)
				return event.eventNumber();

			return null;
		}
	}

	static private class NullHistoryEventToken implements HistoryEventToken
	{
		static final long serialVersionUID = 0L;

		@Override
		final public SequenceNumber retrieve() throws SQLException
		{
			return null;
		}
	}

	private Map<String, SequenceNumber> _nextSequenceMap = new HashMap<String, SequenceNumber>();

	private HistoryEvent getEvent(long historyRecordID)
	{
		Connection connection = null;

		try {
			connection = getConnectionPool().acquire(true);
			return HistoryDatabase.getEvent(connection, historyRecordID);
		} catch (SQLException sqe) {
			_logger.warn(String.format("Error trying to get history event for history record id %d.", historyRecordID), sqe);
			return null;
		} finally {
			getConnectionPool().release(connection);
		}
	}

	private SequenceNumber nextSequenceNumber(Connection connection, String resourceID) throws SQLException
	{
		synchronized (_nextSequenceMap) {
			SequenceNumber next = _nextSequenceMap.get(resourceID);
			if (next == null) {
				next = HistoryDatabase.getNextLargestLevel1SequenceNumber(connection, resourceID);
			}

			SequenceNumber newNext = next.next();
			_nextSequenceMap.put(resourceID, newNext);

			return next;
		}
	}

	@Override
	protected void loadService() throws Throwable
	{
		_logger.info(String.format("Loading %s.", SERVICE_NAME));
		Connection connection = null;

		try {
			connection = getConnectionPool().acquire(true);
			HistoryDatabase.createTables(connection);
		} finally {
			getConnectionPool().release(connection);
		}
	}

	@Override
	protected void startService() throws Throwable
	{
		_logger.info(String.format("Starting %s.", SERVICE_NAME));

		InMemoryAlarmManager.MANAGER.addAlarm(new CleanupAlarmHandler(), CLEANUP_INTERVAL);
	}

	public HistoryContainerService()
	{
		super(SERVICE_NAME);
	}

	final public HistoryEventToken addRecord(String resourceID, SequenceNumber number, HistoryEventCategory category,
		HistoryEventLevel level, Map<String, String> properties, HistoryEventSource eventSource, HistoryEventData eventData,
		Calendar expirationTime)
	{
		return addRecord(resourceID, number, null, category, level, properties, eventSource, eventData, expirationTime);
	}

	final public HistoryEventToken addRecord(String resourceID, SequenceNumber number, Calendar createTimestamp,
		HistoryEventCategory category, HistoryEventLevel level, Map<String, String> properties, HistoryEventSource eventSource,
		HistoryEventData eventData, Calendar expirationTime)
	{
		Connection connection = null;

		try {
			connection = getConnectionPool().acquire(false);

			if (number == null)
				number = nextSequenceNumber(connection, resourceID);

			long ret = HistoryDatabase.addRecord(connection, resourceID, number, createTimestamp, category, level, properties,
				eventSource, eventData, expirationTime);
			connection.commit();
			return new HistoryEventTokenImpl(ret);
		} catch (SQLException sqe) {
			if (!sqe.getSQLState().equals("23505"))
				_logger.warn(String.format("Error trying to add history event record id.  " + "SQL error code is %s.",
					sqe.getSQLState()), sqe);

			return new NullHistoryEventToken();
		} finally {
			getConnectionPool().release(connection);
		}
	}

	final public void deleteRecords(String resourceID)
	{
		Connection connection = null;

		try {
			connection = getConnectionPool().acquire(false);
			deleteRecords(connection, resourceID);
			connection.commit();
		} catch (SQLException sqe) {
			_logger.warn(String.format("Error trying to delete history records for resource %s.", resourceID), sqe);
		} finally {
			getConnectionPool().release(connection);
		}
	}

	final public void deleteRecords(Connection connection, String resourceID) throws SQLException
	{
		HistoryDatabase.deleteRecords(connection, resourceID);
	}

	final public void deleteRecordsLike(Connection connection, String likeConstant)
	{
		try {
			HistoryDatabase.deleteRecordsLike(connection, likeConstant);
		} catch (SQLException sqe) {
			_logger.warn(String.format("Error trying to delete history records for resource like %s.", likeConstant), sqe);
		}
	}

	public Collection<HistoryEvent> getEvents(String resourceID)
	{
		Connection connection = null;

		try {
			connection = getConnectionPool().acquire(true);
			return HistoryDatabase.getEvents(connection, resourceID);
		} catch (SQLException sqe) {
			_logger.warn(String.format("Error trying to get history events for resource %s.", resourceID), sqe);
			return new ArrayList<HistoryEvent>();
		} finally {
			getConnectionPool().release(connection);
		}
	}

	public CloseableIterator<HistoryEvent> iterateEvents(String resourceID) throws SQLException
	{
		CloseableIterator<HistoryEvent> iter;
		Connection connection = null;

		try {
			connection = getConnectionPool().acquire(true);
			iter = HistoryDatabase.iterateEvents(getConnectionPool(), connection, resourceID);
			connection = null;
			return iter;
		} finally {
			if (connection != null)
				getConnectionPool().release(connection);
		}
	}

	public void enqueue(String resourceID, Connection conn) throws SQLException
	{
		HistoryDatabase.addStaleRecord(resourceID, conn);
		conn.commit();

		synchronized (lock) {
			queue.add(resourceID);

		}
	}

	public void loadQueue(Connection connection) throws SQLException
	{

		synchronized (lock) {
			HistoryDatabase.loadStaleHistory(connection, queue);
		}

	}
}