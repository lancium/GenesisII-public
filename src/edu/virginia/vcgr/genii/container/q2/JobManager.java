package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.queue.QueueSecurity;
import edu.virginia.vcgr.genii.queue.JobStateEnumerationType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;

public class JobManager implements Closeable
{
	static private Log _logger = LogFactory.getLog(BESManager.class);
	
	static final private short _MAX_RUN_ATTEMPTS = 10;
	
	volatile private boolean _closed = false;
	
	private ThreadPool _outcallThreadPool;
	private QueueDatabase _database;
	private SchedulingEvent _schedulingEvent;
	
	private HashMap<Long, JobData> _jobsByID = new HashMap<Long, JobData>();
	private HashMap<String, JobData> _jobsByTicket = 
		new HashMap<String, JobData>();
	private TreeMap<SortableJobKey, JobData> _queuedJobs = 
		new TreeMap<SortableJobKey, JobData>();
	private HashMap<Long, JobData> _runningJobs =
		new HashMap<Long, JobData>();
	
	public JobManager(
		ThreadPool outcallThreadPool, QueueDatabase database, SchedulingEvent schedulingEvent,
		Connection connection, DatabaseConnectionPool connectionPool) 
		throws SQLException, ResourceException, 
			ConfigurationException, GenesisIISecurityException
	{
		_database = database;
		_schedulingEvent = schedulingEvent;
		_outcallThreadPool = outcallThreadPool;
		
		loadFromDatabase(connection);
	}
	
	protected void finalize() throws Throwable
	{
		super.finalize();
		
		close();
	}
	
	synchronized public void close() throws IOException
	{
		if (_closed)
			return;
		
		_closed = true;
	}
	
	synchronized private void loadFromDatabase(Connection connection)
		throws SQLException, ResourceException
	{
		Collection<JobData> jobs = _database.loadAllJobs(connection);
		LinkedList<JobData> starting = new LinkedList<JobData>();
		
		for (JobData job : jobs)
		{
			_jobsByID.put(new Long(job.getJobID()), job);
			_jobsByTicket.put(job.getJobTicket(), job);
			
			QueueStates jobState = job.getJobState();
			if (!jobState.isFinalState())
			{
				if (jobState.equals(QueueStates.QUEUED) || 
					jobState.equals(QueueStates.REQUEUED))
				{
					_queuedJobs.put(new SortableJobKey(
						job.getJobID(), job.getPriority(), 
						job.getSubmitTime()), job);
				} else if (jobState.equals(QueueStates.RUNNING))
				{
					_runningJobs.put(new Long(job.getJobID()), job);
				} else
				{
					// If it isn't final, queued, and it isn't running, then we
					// loaded one marked as STARTING, which we can't resolve 
					// (we don't have an EPR to access the job with -- it's 
					// a leak).  We will fail the job
					starting.add(job);
				}
			}
		}
		
		for (JobData job : starting)
		{
			failJob(connection, job.getJobID(), false);
		}
	}
	
	synchronized public void failJob(Connection connection, 
		long jobID, boolean countAsAnAttempt)
		throws SQLException, ResourceException
	{
		JobData job = _jobsByID.get(new Long(jobID));
		if (job == null)
		{
			// don't know where it went, but it's no longer our responsibility.
			return;
		}
		
		if (countAsAnAttempt)
			job.incrementRunAttempts();
		short attempts = job.getRunAttempts();
		QueueStates newState;
		
		if (attempts >= _MAX_RUN_ATTEMPTS)
		{
			// We can't run this job any more.
			newState = QueueStates.ERROR;
		} else
		{
			newState = QueueStates.REQUEUED;
		}
		

		_database.modifyJobState(connection, jobID,
			attempts, newState, new Date(), null, null, null);
		connection.commit();
		
		_runningJobs.remove(new Long(jobID));
		if (newState.equals(QueueStates.REQUEUED))
		{
			_logger.debug("Re-queing job " + jobID);
			_queuedJobs.put(new SortableJobKey(
				jobID, job.getPriority(), job.getSubmitTime()), job);
		} else
		{
			_logger.debug("Failing job " + jobID);
		}
		
		_schedulingEvent.notifySchedulingEvent();
	}
	
	synchronized public void finishJob(Connection connection, long jobID)
		throws SQLException, ResourceException
	{
		JobData job = _jobsByID.get(new Long(jobID));
		if (job == null)
		{
			// don't know where it went, but it's no longer our responsibility.
			return;
		}
		
		job.incrementRunAttempts();
		_database.modifyJobState(connection, jobID,
			job.getRunAttempts(), QueueStates.FINISHED,
			new Date(), null, null, null);
		connection.commit();
		
		_logger.debug("Finished job " + jobID);
		_runningJobs.remove(new Long(jobID));
		_schedulingEvent.notifySchedulingEvent();
	}
	
	synchronized public String submitJob(Connection connection,
		JobDefinition_Type jsdl, short priority) 
		throws SQLException, ConfigurationException, ResourceException
	{
		try
		{
			String ticket = new GUID().toString();
			ICallingContext callingContext = ContextManager.getCurrentContext(false);
			Collection<Identity> identities = QueueSecurity.getCallerIdentities();
			QueueStates state = QueueStates.QUEUED;
			Date submitTime = new Date();
			
			long jobID = _database.submitJob(
				connection, ticket, priority, jsdl, callingContext, identities, 
				state, submitTime);
			connection.commit();
			
			_logger.debug("Submitted job \"" + ticket + "\" as job number " + jobID);
			
			JobData job = new JobData(
				jobID, ticket, priority, state, submitTime, (short)0);
			_jobsByID.put(new Long(jobID), job);
			_jobsByTicket.put(ticket, job);
			_queuedJobs.put(new SortableJobKey(jobID, priority, submitTime),
				job);
			_schedulingEvent.notify();
			
			return ticket;
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to submit job.", ioe);
		}
	}
	
	synchronized public Collection<ReducedJobInformationType> listJobs(
		Connection connection) throws SQLException, ResourceException
	{
		Collection<ReducedJobInformationType> ret = 
			new LinkedList<ReducedJobInformationType>();
		
		try
		{
			for (Long jobID : _jobsByID.keySet())
			{
				ret.add(_database.getReducedInformation(
					connection, jobID.longValue()));
			}
			
			return ret;
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ResourceException("Unable to list jobs.", cnfe);
		}
		catch (IOException ioe)
		{
			throw new ResourceException("Unable to list jobs.", ioe);
		}
	}
}