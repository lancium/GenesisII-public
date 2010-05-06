package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Counter;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.cservices.gridlogger.GridLogDevice;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.q2.matching.JobResourceRequirements;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

/**
 * The scheduler class is another manager used by the queue that actively
 * looks for opportunities to match jobs to resources and then launches them.
 * 
 * @author mmm2a
 */
public class Scheduler implements Closeable
{
	static final private String IS_SCHEDULING_PROPERTY = 
		"edu.virginia.vcgr.genii.container.q2.is-scheulding";
	
	static private Log _logger = LogFactory.getLog(Scheduler.class);
	static private GridLogDevice _jobLogger = new GridLogDevice(Scheduler.class);
	
	volatile private boolean _closed = false;
	
	private String _queueID;
	
	private SchedulingEvent _schedulingEvent;
	private DatabaseConnectionPool _connectionPool;
	
	private volatile boolean _isSchedulingJobs;
	
	private JobManager _jobManager;
	private BESManager _besManager;
	
	private void loadIsSchedulingJobs() throws SQLException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			String str = (String)BasicDBResource.getProperty(
				connection, _queueID,
				IS_SCHEDULING_PROPERTY);
			_isSchedulingJobs = (str == null) ? true : Boolean.parseBoolean(str);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	public Scheduler(String queueID,
		SchedulingEvent schedulingEvent, 
		DatabaseConnectionPool connectionPool,
		JobManager jobManager, BESManager besManager) throws SQLException
	{
		_schedulingEvent = schedulingEvent;
		_connectionPool = connectionPool;
		
		_jobManager = jobManager;
		_besManager = besManager;
		_queueID = queueID;
		
		loadIsSchedulingJobs();
		
		Thread schedulerThread = new Thread(new SchedulerWorker());
		schedulerThread.setDaemon(true);
		schedulerThread.setName("Queue Scheduler Worker");
		schedulerThread.start();
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
	}
	
	/**
	 * Match queued jobs against available slots.
	 * 
	 * @throws ResourceException
	 */
	private void scheduleJobs() throws ResourceException
	{
		
		if (!_isSchedulingJobs)
		{
			_logger.info("Skipping a scheduling loop because the \"" +
				"isScheduling\" property of the queue is turned off.");
			return;
		}
		
		HashMap<Long, ResourceSlots> slots = 
			new HashMap<Long, ResourceSlots>();
		HashMap<JobResourceRequirements, Counter> jobCounts =
			new HashMap<JobResourceRequirements, Counter>();
		
		try
		{
			/* First, we have to find all bes managers that are
			 * availble to accept jobs.  In this case, available simply means
			 * that they are responsive to communication and accepting activities
			 * and have more then 0 slots allocated.  It does NOT imply that the
			 * slots aren't being used.  We determine that later.
			 */
			synchronized(_besManager)
			{
				/* Get all available resources */
				Collection<BESData> availableResources = 
					_besManager.getAvailableBESs();
	
				/* If we didn't get any resources back, then there's no reason
				 * to continue.
				 */
				if (availableResources.size() == 0)
					return;
				
				/* Now we go through the list and get rid of all resources that
				 * had no slots allocated.
				 */
				for (BESData data : availableResources)
				{
					ResourceSlots rs = new ResourceSlots(data);
					if (rs.slotsAvailable() > 0)
						slots.put(new Long(data.getID()), rs);
				}
			}
			
			/* If there are no slots available, then we are done. */
			if (slots.size() <= 0)
				return;
			
			// We've left the synchronized block for BESs, so we have to keep in
			// mind that they could dissapear out from under us during this time.
			
			synchronized(_jobManager)
			{
				/* If the job manager has no queued jobs, then we don't need to 
				 * continue.
				 */
				if (!_jobManager.hasQueuedJobs())
					return;
				
				/* Ask the job manager to remove all slots from the slot list that
				 * are currently being used.
				 */ 
				_jobManager.recordUsedSlots(slots);
				
				/* If there are no slots left, we're done. */
				if (slots.isEmpty())
					return;
				
				// At this point, we have slots available (probably) and we have 
				// jobs to run
				
				ResourceMatcher matcher = new ResourceMatcher();
				Collection<ResourceMatch> matches = new LinkedList<ResourceMatch>();
				Iterator<ResourceSlots> slotIter = null;
				ResourceMatch match;
				
				/* We are now going to match as many jobs to as many slots as 
				 * possible. To make this as efficient as possible, we keep track
				 * of the iterator and continually re-iterate until we go through
				 * all the jobs waiting for a slot, or we run out of resources to
				 * scheduling against.  We are also going to keep track of the next
				 * job that should be scheduled, but can't be scheduled now (for
				 * exponential backoff purposes).
				 */
				Date now = new Date();
				Date nextScheduledEvent = null;
				for (JobData queuedJob : _jobManager.getQueuedJobs())
				{
					match = null;
					if (!queuedJob.canRun(now))
					{
						Date nextRun = queuedJob.getNextCanRun();
						if (nextRun != null)
						{
							if (nextScheduledEvent == null)
								nextScheduledEvent = nextRun;
							else
							{
								if (nextScheduledEvent.after(nextRun))
									nextScheduledEvent = nextRun;
							}
						}
						
						continue;
					}
					
					JobResourceRequirements requirements = null;
					
					try
					{
						requirements = queuedJob.getResourceRequirements(_jobManager);
					}
					catch (Throwable cause)
					{
						_logger.warn(
							"Error trying to get job resource requirements for matching.", 
							cause);
						requirements = new JobResourceRequirements();
					}
					
					/* If we haven't created an iterator yet, do so now. */
					if (slotIter == null)
					{
						/* If there aren't any slots, we're done */
						if (slots.isEmpty())
							break;
						
						slotIter = slots.values().iterator();
						
						/* Try to find a match */
						match = findSlot(matcher, queuedJob, requirements, slotIter);
					} else
					{
						/* If we got here, then we already had an iterator 
						 * from before.  Try to find a match with it. */
						match = findSlot(matcher, queuedJob, requirements, slotIter);
						
						/* If we couldn't find a match, it may have been the case
						 * that we simply had already passed a potential match with
						 * the iterator before getting here, so give the iterator
						 * a new chance from the begining.
						 */
						if (match == null)
						{
							/* If there are no slots available, we're done. */
							if (slots.isEmpty())
								break;
							
							/* Create a new iterator and try to find 
							 * a match again. */
							slotIter = slots.values().iterator();
							match = findSlot(matcher, queuedJob, requirements, slotIter);
						}
					}
					
					/* If we found a match, then go ahead and add it 
					 * to our list. */
					if (match != null)
					{
						_jobLogger.log(queuedJob.gridLogTargets(),
							String.format("Matched job against resource \"%s\".",
								_besManager.getBESName(match.getBESID())));
						matches.add(match);
					} else
					{
						_jobLogger.log(queuedJob.gridLogTargets(),
							"Unable to find any suitable matches at the moment." +
							"  Will try again later.");
						Counter c = jobCounts.get(requirements);
						if (c == null)
							jobCounts.put(requirements, c = new Counter());
						c.modify(1);
					}		
				}
				
				_schedulingEvent.setScheduledEvent(nextScheduledEvent);
				
				/* OK, now that we have a list of matches, go ahead and try to
				 * start the jobs.
				 */
				Connection connection = null;
				try
				{
					/* Acquire a new connection from the pool */
					connection = _connectionPool.acquire(false);
					
					/* And start the jobs. */
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
		finally
		{
			for (Map.Entry<JobResourceRequirements, Counter> entry : jobCounts.entrySet())
			{
				_logger.debug(String.format(
					"%d jobs failed to match any resources with requirements %s",
					entry.getValue().get(), entry.getKey()));
			}
		}
	}
	
	/**
	 * Find a resource that matches the given job.
	 * 
	 * @param matcher A resource matcher that determines whether or not
	 * jobs match given resources.
	 * @param queuedJob The job to match against.
	 * @param slots The list of slots (iterator) against which to find a match.
	 * 
	 * @return The match (if one was found), otherwise null.
	 */
	private ResourceMatch findSlot(ResourceMatcher matcher, JobData queuedJob,
		JobResourceRequirements requirements, Iterator<ResourceSlots> slots)
	{
		if (requirements != null)
		{
			while (slots.hasNext())
			{
				ResourceSlots rSlots = slots.next();
				
				try
				{
					if (matcher.matches(requirements, 
						_besManager.getBESInformation(rSlots.getBESID())))
					{
						/* If there was a match, reserve the slot */
						rSlots.reserveSlot();
						
						/* If we just reserved the last available slot, take it out
						 * of the list.
						 */
						if (rSlots.slotsAvailable() <= 0)
							slots.remove();
						return new ResourceMatch(queuedJob.getJobID(), 
							rSlots.getBESID());
					}
				}
				catch (Throwable cause)
				{
					_logger.warn("Error trying to match job to resource.", cause);
				}
			}
		}
		
		return null;
	}
	
	/**
	 * This class is used by the scheduler to wait on scheduling opportunities
	 * and the start a scheduling process.
	 * 
	 * @author mmm2a
	 */
	private class SchedulerWorker implements Runnable
	{
		public void run()
		{
			long startTime = 0L;
			
			/* A small hack, but go ahead and pre-notify ourselves that
			 * there might be a scheduling opportunity.  This bootstraps
			 * the scheduler for when it is first loaded.  If we just loaded
			 * state from the database, this will start the scheduling process.
			 */
			_schedulingEvent.notifySchedulingEvent();
			
			while (!_closed)
			{
				try
				{
					/* Wait until there is a scheduling opportunity */
					_schedulingEvent.waitSchedulingEvent();
					try
					{
						/* Now that we have an opportunity, go ahead and
						 * schedule some jobs if we can.
						 */
						startTime = System.currentTimeMillis();
						scheduleJobs();
					}
					catch (Throwable cause)
					{
						_logger.warn(
							"An exception occurred while scheduling new " +
							"jobs to run on the queue.", cause);
					}
					finally
					{
						_logger.debug(String.format(
							"It took %d milliseconds to run the scheduling loop.",
							(System.currentTimeMillis() - startTime)));
					}
				}
				catch (InterruptedException ie)
				{
					Thread.interrupted();
				}
			}
		}
	}
	
	public void storeIsScheduling(boolean isScheduling) 
		throws SQLException
	{
		Connection connection = null;
		
		try
		{
			connection = _connectionPool.acquire(true);
			BasicDBResource.setProperty(
				connection, _queueID, IS_SCHEDULING_PROPERTY,
				isScheduling);
			_isSchedulingJobs = isScheduling;
			if (_isSchedulingJobs)
				_schedulingEvent.notifySchedulingEvent();
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
	
	final public boolean isSchedulingJobs()
	{
		return _isSchedulingJobs;
	}
}