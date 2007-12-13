package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class Scheduler implements Closeable
{
	static private Log _logger = LogFactory.getLog(Scheduler.class);
	
	volatile private boolean _closed = false;
	
	private SchedulingEvent _schedulingEvent;
	private DatabaseConnectionPool _connectionPool;
	
	private JobManager _jobManager;
	private BESManager _besManager;
	private Thread _schedulerThread;
	
	public Scheduler(
		SchedulingEvent schedulingEvent, 
		DatabaseConnectionPool connectionPool,
		JobManager jobManager, BESManager besManager)
	{
		_schedulingEvent = schedulingEvent;
		_connectionPool = connectionPool;
		
		_jobManager = jobManager;
		_besManager = besManager;
		
		_schedulerThread = new Thread(new SchedulerWorker());
		_schedulerThread.setDaemon(true);
		_schedulerThread.setName("Queue Scheduler Worker");
		_schedulerThread.start();
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	synchronized public void close() throws IOException
	{
		if (_closed)
			return;
		
		_closed = true;
		_schedulerThread.interrupt();
	}
	
	private void scheduleJobs() throws ResourceException
	{
		HashMap<Long, ResourceSlots> slots = 
			new HashMap<Long, ResourceSlots>();
		
		synchronized(_besManager)
		{
			Collection<BESData> availableResources = 
				_besManager.getAvailableBESs();
			if (availableResources.size() == 0)
				return;
			
			for (BESData data : _besManager.getAvailableBESs())
			{
				ResourceSlots rs = new ResourceSlots(data);
				if (rs.slotsAvailable() > 0)
					slots.put(new Long(data.getID()), rs);
			}
		}
		
		// We've left the synchronized block for BESs, so we have to keep in
		// mind that they could dissapear out from under us during this time.
		
		synchronized(_jobManager)
		{
			if (!_jobManager.hasQueuedJobs())
				return;
			
			_jobManager.recordUsedSlots(slots);
			if (slots.isEmpty())
				return;
			
			// At this point, we have slots available (probably) and we have 
			// jobs to run
			ResourceMatcher matcher = new ResourceMatcher();
			Collection<ResourceMatch> matches = new LinkedList<ResourceMatch>();
			Iterator<ResourceSlots> slotIter = null;
			ResourceMatch match;
			
			for (JobData queuedJob : _jobManager.getQueuedJobs())
			{
				match = null;
				
				if (slotIter == null)
				{
					if (slots.isEmpty())
						break;
					slotIter = slots.values().iterator();
					match = findSlot(matcher, queuedJob, slotIter);
				} else
				{
					match = findSlot(matcher, queuedJob, slotIter);
					if (match == null)
					{
						if (slots.isEmpty())
							break;
						slotIter = slots.values().iterator();
						match = findSlot(matcher, queuedJob, slotIter);
					}
				}
				
				if (match != null)
				{
					matches.add(match);
				}
			}
			
			Connection connection = null;
			try
			{
				connection = _connectionPool.acquire();
				_jobManager.startJobs(connection, matches);
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to schedule jobs.", cause);
			}
			finally
			{
				_connectionPool.release(connection);
			}
		}
	}
	
	private ResourceMatch findSlot(ResourceMatcher matcher, JobData queuedJob, 
		Iterator<ResourceSlots> slots)
	{
		while (slots.hasNext())
		{
			ResourceSlots rSlots = slots.next();
			if (matcher.matches(queuedJob.getJobID(), rSlots.getBESID()))
			{
				rSlots.reserveSlot();
				if (rSlots.slotsAvailable() <= 0)
					slots.remove();
				return new ResourceMatch(queuedJob.getJobID(), rSlots.getBESID());
			}
		}
		
		return null;
	}
	
	private class SchedulerWorker implements Runnable
	{
		public void run()
		{
			_schedulingEvent.notifySchedulingEvent();
			
			while (!_closed)
			{
				try
				{
					if (_schedulingEvent.waitSchedulingEvent())
					{
						try
						{
							scheduleJobs();
						}
						catch (Throwable cause)
						{
							_logger.warn(
								"An exception occurred while scheduling new " +
								"jobs to run on the queue.", cause);
						}
					}
				}
				catch (InterruptedException ie)
				{
					Thread.interrupted();
				}
			}
		}
	}
}