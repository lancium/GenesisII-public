package edu.virginia.vcgr.genii.container.cservices.gridlogger;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.spi.LoggingEvent;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.gridlog.GridLogTarget;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;
import edu.virginia.vcgr.genii.container.cservices.ContainerServicePropertyListener;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;

public class GridLoggerContainerService extends AbstractContainerService
{
	static private Log _logger = LogFactory.getLog(GridLoggerContainerService.class);

	static public final String SERVICE_NAME = "Grid Logger";
	
	static public final String NUM_RETRIES_PROPERTY = 
		"edu.virginia.vcgr.genii.cservices.gridlogger.num-tries";
	static public final String BACKOFF_BASE_PROPERTY = 
		"edu.virginia.vcgr.genii.cservices.gridlogger.backoff-base";
	
	static public final int DEFAULT_NUM_RETRIES = 8;
	static public final long DEFAULT_BACKOFF_BASE = 1024 * 16;
	
	private int _numRetries = DEFAULT_NUM_RETRIES;
	private long _backoffBase = DEFAULT_BACKOFF_BASE;
	
	private PriorityQueue<GridLogEvent> _events =
		new PriorityQueue<GridLogEvent>(32, GridLogEvent.COMPARATOR);
	
	private void removeEvent(long id)
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try
		{
			conn = getConnectionPool().acquire(true);
			stmt = conn.prepareStatement(
				"DELETE FROM outgoinglogevents WHERE id = ?");
			stmt.setLong(1, id);
			stmt.executeUpdate();
			
			conn.commit();
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to clean up log event.", cause);
		}
		finally
		{
			StreamUtils.close(stmt);
			getConnectionPool().release(conn);
		}
	}
	
	private void reinsertEvent(GridLogEvent event)
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try
		{
			conn = getConnectionPool().acquire(true);
			stmt = conn.prepareStatement(
				"UPDATE outgoinglogevents SET numattempts = ? WHERE id = ?");
			stmt.setShort(1, (short)event.numAttempts());
			stmt.setLong(2, event.id());
			stmt.executeUpdate();
			conn.commit();
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to modify log event.", cause);
		}
		finally
		{
			StreamUtils.close(stmt);
			getConnectionPool().release(conn);
		}
		
		synchronized(_events)
		{
			_events.add(event);
			_events.notifyAll();
		}
	}
	
	public GridLoggerContainerService(Properties constructionProperties)
	{
		super(SERVICE_NAME);
		
		String value = constructionProperties.getProperty(
			BACKOFF_BASE_PROPERTY);
		if (value != null)
			_backoffBase = Long.parseLong(value);
		
		value = constructionProperties.getProperty(NUM_RETRIES_PROPERTY);
		if (value != null)
			_numRetries = Integer.parseInt(value);
	}
	
	@Override
	protected void loadService() throws Throwable
	{
		_logger.info(String.format("Loading %s.", SERVICE_NAME));
		
		getContainerServicesProperties().addPropertyChangeListener(
			new PropertyChangeListener());
		
		Connection conn = null;
		
		try
		{
			conn = getConnectionPool().acquire(true);
			DatabaseTableUtils.createTables(conn, false,
				"CREATE TABLE outgoinglogevents (" +
					"id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
					"target BLOB(2G) NOT NULL, " +
					"content BLOB(2G) NOT NULL, " +
					"numattempts SMALLINT NOT NULL)");
			conn.commit();
		}
		finally
		{
			getConnectionPool().release(conn);
		}
	}

	@Override
	protected void startService() throws Throwable
	{
		_logger.info(String.format("Starting %s.", SERVICE_NAME));
		
		// setupLog4jAppender();
		
		Thread th = new Thread(new LogEventSender(), "Log Event Sender");
		th.setDaemon(true);
		th.start();
	}
	
	public void logEvent(LoggingEvent event, Collection<GridLogTarget> targets)
	{
		// We can't use log4j in this method, or we'll have an infinite loop.
		
		/* Was this all causing unnecessary load on the Queue?  We're
		 * commenting it out to find out.
		 * Mark Morgan
		 *
		Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement qStmt = null;
		ResultSet rs = null;
		Collection<GridLogEvent> events = new Vector<GridLogEvent>(targets.size());
		
		try
		{
			conn = getConnectionPool().acquire(false);
			stmt = conn.prepareStatement(
				"INSERT INTO outgoinglogevents " +
					"(target, content, numattempts) VALUES (?, ?, ?)");
			qStmt = conn.prepareStatement("values IDENTITY_VAL_LOCAL()");
			Blob content = DBSerializer.toBlob(event, 
				"outgoinglogevents", "content");
			for (GridLogTarget target : targets)
			{
				stmt.setBlob(1, DBSerializer.toBlob(
					target, "outgoinglogevents", "target"));
				stmt.setBlob(2, content);
				stmt.setShort(3, (short)0);
				
				stmt.executeUpdate();
				rs = qStmt.executeQuery();
				rs.next();
				events.add(new GridLogEvent(
					rs.getLong(1), target, event, (short)0));
				rs.close();
				rs = null;
			}
			conn.commit();
			
			synchronized(_events)
			{
				for (GridLogEvent gle : events)
					_events.add(gle);

				_events.notifyAll();
			}
		}
		catch (SQLException sqe)
		{
			// Unable to log event.
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			StreamUtils.close(qStmt);
			getConnectionPool().release(conn);
		}
		*/
	}
	
	/*
	private void setupLog4jAppender()
	{
		GridLoggerAppender appender = new GridLoggerAppender();
		appender.setThreshold(Level.ALL);
		Logger.getRoot().addAppender(appender);
	}
	*/
	
	/*
	private class GridLoggerAppender extends AppenderSkeleton
	{
		@Override
		protected void append(LoggingEvent event)
		{
			try
			{
				Collection<GridLogTarget> targets =
					GridLogUtils.getTargets();
				logEvent(event, targets);
			}
			catch (Throwable cause)
			{
				// Can't really print anything in here.
			}
		}
		
		@Override
		public void close()
		{
			// We can safely ignore this.
		}
		
		@Override
		final public boolean requiresLayout()
		{
			return false;
		}
	}
	*/
	
	private class PropertyChangeListener
		implements ContainerServicePropertyListener
	{
		@Override
		public void propertyChanged(String propertyName, Serializable newValue)
		{
			if (propertyName.equals(NUM_RETRIES_PROPERTY))
				_numRetries = Integer.parseInt((String)newValue);
			else if (propertyName.equals(BACKOFF_BASE_PROPERTY))
				_backoffBase = Long.parseLong((String)newValue);
		}
	}
	
	private class LogEventSender implements Runnable
	{
		@Override
		public void run()
		{
			long now;
			
			while (true)
			{
				now = System.currentTimeMillis();
				synchronized(_events)
				{
					while (!_events.isEmpty() && 
						_events.peek().nextAttempt() <= now)
					{
						getExecutor().submit(new SendWorker(_events.remove()));
					}
					
					try
					{
						if (_events.isEmpty())
							_events.wait();
						else
							_events.wait(_events.peek().nextAttempt() - now);
					}
					catch (InterruptedException ie)
					{
						// Ignore
					}
				}
			}
		}
	}
	
	private class SendWorker implements Runnable
	{
		private GridLogEvent _event;
		
		private SendWorker(GridLogEvent event)
		{
			_event = event;
		}
		
		@Override
		public void run()
		{
			try
			{
				/* Was this causing the Queue to have high load?
				GridLogTarget target = _event.target();
				GridLogPortType stub = target.connect();
				stub.appendToLog(
					new AppendToLogRequestType(target.loggerID(),
						GridLogUtils.convert(_event.content()),
						Hostname.getLocalHostname().toString()));
				*/
				removeEvent(_event.id());
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to send log event.", cause);
				if (_event.numAttempts(1) >= _numRetries)
				{
					_logger.warn("Giving up on event.");
					removeEvent(_event.id());
				} else
				{
					_event.nextAttempt(_backoffBase);
					reinsertEvent(_event);
				}
			}
		}
	}
}