package edu.virginia.vcgr.genii.container.cservices.percall;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.NavigableSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;

public class PersistentOutcallContainerService extends AbstractContainerService
{
	static final public String SERVICE_NAME = "Persistent Outcall Container Service";
	
	static private Log _logger = LogFactory.getLog(
		PersistentOutcallContainerService.class);
	
	/* 1 month */
	static final private long MAX_TIME_TO_LIVE = 1000L * 60 * 60 * 24 * 30;
	
	private NavigableSet<PersistentOutcallEntry> _entries;
	
	public PersistentOutcallContainerService()
	{
		super(SERVICE_NAME);
	}
	
	final public boolean schedule(
		OutcallActor actor, AttemptScheduler scheduler,
		EndpointReferenceType target, ICallingContext callingContext,
		GeniiAttachment attachment)
	{
		Connection connection = null;
		
		try
		{
			connection = getConnectionPool().acquire(false);
			PersistentOutcallEntry entry = PersistentOutcallDatabase.add(
				connection, target, callingContext, actor, scheduler, attachment);
			connection.commit();
			
			synchronized(_entries)
			{
				_entries.add(entry);
				_entries.notify();
			}
			
			return true;
		}
		catch (SQLException e)
		{
			_logger.warn(
				"Unable to add persistent outcall to container service.", e);
			return false;
		}
		finally
		{
			getConnectionPool().release(connection);
		}
	}

	@Override
	final protected void loadService() throws Throwable
	{
		_logger.info(String.format("Loading %s.", SERVICE_NAME));
		Connection connection = null;
		
		try
		{
			connection = getConnectionPool().acquire(true);
			PersistentOutcallDatabase.createTables(connection);
			_entries = PersistentOutcallDatabase.readTable(connection);
			_logger.debug(String.format(
				"%s loaded %d entries", SERVICE_NAME, _entries.size()));
		}
		finally
		{
			getConnectionPool().release(connection);
		}
	}

	@Override
	final protected void startService() throws Throwable
	{
		_logger.info(String.format("Starting %s.", SERVICE_NAME));
		
		Thread th = new Thread(new OutcallWorker(),
			"Persisent Outcall Worker");
		th.setDaemon(true);
		th.setPriority(Thread.MIN_PRIORITY);
		th.start();
	}
	
	final private void reAdd(Connection connection, PersistentOutcallEntry entry)
	{
		try
		{
			Calendar now = Calendar.getInstance();
			if (now.getTimeInMillis() - entry.createTime().getTimeInMillis() > MAX_TIME_TO_LIVE)
			{
				_logger.warn("A persistent outcall has exceeded the maximum allowed time to live -- it is being removed.");
				PersistentOutcallDatabase.remove(connection, entry);
				return;
			}
			
			entry.numAttempts(entry.numAttempts() + 1);
			Calendar nextAttempt = 
				entry.scheduler().nextAttempt(
						Calendar.getInstance(), entry.numAttempts());
			if (nextAttempt == null)
			{
				_logger.warn(
					"Giving up on persistent outcall that we could never make.");
				PersistentOutcallDatabase.remove(connection, entry);
			} else
			{
				_logger.debug("PersistentOutcall: Next attempt at " +
					new SimpleDateFormat("HH:mm:ss").format(nextAttempt.getTime()));
				entry.nextAttempt(nextAttempt);
				PersistentOutcallDatabase.update(connection, entry);
				synchronized(_entries)
				{
					_entries.add(entry);
				}
			}
		}
		catch (Throwable cause)
		{
			_logger.warn(
				"Unable to re-add/remove persistent outcall entry into/from database.",
				cause);
		}
	}
	
	final private void doOutcall(PersistentOutcallEntry entry)
	{
		Connection connection = null;
		
		_logger.debug("Doing persistent outcall.");
		
		try
		{
			connection = getConnectionPool().acquire(false);
			CommunicationInformation commInfo = 
				PersistentOutcallDatabase.getCommunicationInformation(
					connection, entry);
			connection.commit();
			if (commInfo.outcallActor.enactOutcall(
				commInfo.callingContext, commInfo.targetEPR, commInfo.attachment))
			{
				_logger.debug("Successfully made persistent outcall -- forgetting it.");
				
				PersistentOutcallDatabase.remove(connection, entry);
				connection.commit();
				return;
			}
			
			_logger.warn("PersistentOutcall: response was Try Again");
			reAdd(connection ,entry);
			connection.commit();
		}
		catch (Throwable cause)
		{
			_logger.warn(
				"Persistent outcall service tried to make outcall, but " +
				"got an exception -- putting it back in the list.", cause);
			reAdd(connection, entry);
			try
			{
				connection.commit();
			}
			catch (SQLException sqe)
			{
				_logger.error(
					"Unable to commit connection for persistent outcall db.", sqe);
			}
		}
		finally
		{
			getConnectionPool().release(connection);
		}
	}
	
	private class OutcallWorker implements Runnable
	{
		@Override
		final public void run()
		{
			while (true)
			{
				try
				{
					PersistentOutcallEntry entry = null;
					
					Calendar now = Calendar.getInstance();
					Calendar next = null;
					
					synchronized(_entries)
					{
						if (!_entries.isEmpty())
							next = _entries.first().nextAttempt();
						
						if (next == null)
							_entries.wait();
						else if (next.after(now))
							_entries.wait(next.getTimeInMillis() - now.getTimeInMillis());
						else
							entry = _entries.pollFirst();
					}
				
					if (entry != null)
						doOutcall(entry);
				}
				catch (Throwable cause)
				{
					_logger.warn("Persistent outcall service saw an exception.", cause);
				}
			}
		}
	}
	
	static public boolean schedulePersistentOutcall(
		OutcallActor actor, AttemptScheduler scheduler,
		EndpointReferenceType target, ICallingContext callingContext)
	{
		PersistentOutcallContainerService service = 
			ContainerServices.findService(
				PersistentOutcallContainerService.class);
		if (service != null)
			return service.schedule(actor, scheduler, target, callingContext, null);
		else
			_logger.warn("Unable to find persistent oucall service.");
		
		return false;
	}
}
