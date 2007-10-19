package edu.virginia.vcgr.genii.container.queue;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.BESPortType;
import org.ggf.bes.factory.ActivityDocumentType;
import org.ggf.bes.factory.CreateActivityType;
import org.ggf.bes.factory.GetActivityStatusResponseType;
import org.ggf.bes.factory.ActivityStatusType;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.utils.BoundedBlockingQueue;
import edu.virginia.vcgr.genii.client.utils.BoundedThreadPool;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class JobManager implements Runnable
{
	static private Log _logger = LogFactory.getLog(JobManager.class);
	
	static private final long _DEFAULT_UPDATE_CYCLE = 1000L * 30;
	static private final int _NUM_OUTSTANDING_THREADS = 1;  // TODO: fix the horriblenes  
															// (e.g., NPEs from accessing
															// state while its being destroyed
															// out from under you) that result
															// when increasing the degree of 
															// mulitprogramming > 1
	static private final int _MAX_FAILED_ATTEMPTS = 10;
	
	static private DatabaseConnectionPool _connectionPool = null;
	static private HashMap<String, JobManager> _managers =
		new HashMap<String, JobManager>();
	
	static final private String _FIND_QUEUES_STMT =
		"SELECT queueid FROM queueresources GROUP BY queueid";
	static public void startAllManagers(DatabaseConnectionPool connectionPool)
		throws SQLException, ResourceException
	{
		_connectionPool = connectionPool;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			conn = _connectionPool.acquire();
			stmt = conn.prepareStatement(_FIND_QUEUES_STMT);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				String queueid = rs.getString(1);
				_logger.info("Starting job manager for queue " + queueid);
				createManager(queueid);
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(conn);
		}
	}
	
	static public JobManager getManager(String queueID)
		throws ResourceException
	{
		JobManager manager;
		synchronized(_managers)
		{
			manager = _managers.get(queueID);
		}
		
		if (manager == null)
			manager = createManager(queueID);
		
		return manager;
	}
	
	static public JobManager createManager(String queueID)
		throws ResourceException
	{
		JobManager manager;
		synchronized(_managers)
		{
			manager = _managers.get(queueID);
			if (manager != null)
				throw new ResourceException("Job Manager for queue " +
					queueID + " already exists.");
			manager = new JobManager(queueID);
			_managers.put(queueID, manager);
		}
		
		return manager;
	}
	
	static public void deleteManager(String queueID)
		throws ResourceException
	{
		JobManager manager;
		synchronized(_managers)
		{
			manager = _managers.remove(queueID);
		}
		
		if (manager == null)
			throw new ResourceException("Couldn't find job manager for queue "
				+ queueID);
		
		manager.destroy();
	}
	
	private String _queueID;
	private Thread _myThread;
	volatile private boolean _finished = false;
	private BoundedThreadPool _threadPool = null;
	private SchedulingMarker _schedulingOpportunity = new SchedulingMarker();
	
	private JobManager(String queueID)
	{
		_threadPool = new BoundedThreadPool(
			new BoundedBlockingQueue<Runnable>(_NUM_OUTSTANDING_THREADS));
		ThreadFactory factory = (ThreadFactory)NamedInstances.getRoleBasedInstances(
			).lookup("thread-factory");
		
		_queueID = queueID;
		_myThread = factory.newThread(this);
		_myThread.setDaemon(false);
		_myThread.setName("Queue Job Manager");
		_myThread.start();
	}
	
	protected void finalize() throws Throwable
	{
		try
		{
			destroy();
		}
		finally
		{
			super.finalize();
		}
	}
	
	synchronized private void destroy()
	{
		if (_myThread != null)
		{
			_finished = true;
			_myThread.interrupt();
			_myThread = null;
			_threadPool.close();
		}
	}
	
	public void run()
	{
		requeueStartingJobs();
		
		while (!_finished)
		{
			try
			{
				checkJobs();
				synchronized(_schedulingOpportunity)
				{
					_schedulingOpportunity.wait(_DEFAULT_UPDATE_CYCLE);
				}
			}
			catch (InterruptedException ie)
			{
				Thread.interrupted();
			}
		}
	}
	
	public void jobSchedulingOpportunity()
	{
		_schedulingOpportunity.haveOpportunity(true);
		synchronized(_schedulingOpportunity)
		{
			_schedulingOpportunity.notifyAll();
		}
	}
	
	static final private String _LIST_ALL_RUNNING_JOBS =
		"SELECT a.jobid, a.resourceid, a.jobendpoint, b.endpoint FROM " +
			"(SELECT jobid, resourceid, jobendpoint FROM queueactivejobs WHERE " +
				"queueid = ? AND jobendpoint IS NOT NULL) AS a INNER JOIN " +
			"queueresourceinfo AS b ON a.resourceid = b.resourceid";
	private void checkJobs()
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		handleSchedulingOpportunity();
		_logger.debug("Checking status of all jobs.");
		try
		{
			conn = _connectionPool.acquire();
			stmt = conn.prepareStatement(_LIST_ALL_RUNNING_JOBS);
			stmt.setString(1, _queueID);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				try
				{
					checkJobStatus(conn, 
						rs.getInt(1), rs.getInt(2), 
						EPRUtils.fromBlob(rs.getBlob(3)),
						EPRUtils.fromBlob(rs.getBlob(4)));
				}
				catch (ResourceException re)
				{
					_logger.error("Unable to read resource endpoint from database.",
						re);
				}
				
				handleSchedulingOpportunity();
			}
			conn.commit();
		}
		catch (SQLException sqe)
		{
			_logger.error("Unable to query the database.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
			StreamUtils.close(rs);
			_connectionPool.release(conn);
		}
		handleSchedulingOpportunity();
	}
	
	private void handleSchedulingOpportunity()
	{
		if (_schedulingOpportunity.haveOpportunity())
		{
			scheduleJobs();
			_schedulingOpportunity.haveOpportunity(false);
		}
	}

/*	
	static private final String _FIND_JOBS_STMT =
		"SELECT b.jobid, b.callingcontext, b.jsdl, b.failedattempts FROM " +
			"(SELECT jobid FROM queuejobs WHERE queueid = ?) AS a " +
			"INNER JOIN " +
			"(SELECT jobid, callingcontext, jsdl, failedattempts FROM " +
				"queuejobinfo WHERE state IN ('QUEUED', 'REQUEUED') " +
				"ORDER BY priority DESC, submittime) AS b " +
			"ON a.jobid = b.jobid";
*/
	static private final String _FIND_JOBS_STMT =
		"SELECT b.jobid, b.callingcontext, b.jsdl, b.failedattempts FROM (SELECT jobid FROM queuejobs WHERE queueid = ?) AS a INNER JOIN (SELECT jobid, callingcontext, jsdl, failedattempts, priority, submittime FROM queuejobinfo WHERE state IN ('QUEUED', 'REQUEUED')) AS b ON a.jobid = b.jobid ORDER BY b.priority DESC, b.submittime";

	private void scheduleJobs()
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ResourceMatcher rMatcher = null;
		
		try
		{
			conn = _connectionPool.acquire();
			rMatcher = new ResourceMatcher(conn, _queueID);
			
			stmt = conn.prepareStatement(_FIND_JOBS_STMT);
			stmt.setString(1, _queueID);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				JobRequest request = new JobRequest(rs);
				ResourceSlot matchingSlot = rMatcher.match(request);
				
				if (matchingSlot == null)
					break;
				
				startJob(conn, request, matchingSlot);
			}
		}
		catch (SQLException sqe)
		{
			_logger.error("Error interacting with the database.", sqe);
		}
		finally
		{
			StreamUtils.close(rMatcher);
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(conn);
		}
	}
	
	static final private String _GET_JOB_CALLING_CONTEXT =
		"SELECT callingcontext, failedattempts FROM queuejobinfo WHERE jobid = ?";
	private void checkJobStatus(Connection conn,
		int jobID, int resourceID, 
		EndpointReferenceType jobEndpoint,
		EndpointReferenceType containerEndpoint)
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.prepareStatement(_GET_JOB_CALLING_CONTEXT);
			stmt.setInt(1, jobID);
			rs = stmt.executeQuery();
			if (!rs.next())
			{
				_logger.error("Unable to find job " + jobID);
				return;
			}

			int failedAttempts = rs.getInt(2);
			ICallingContext callingContext = (ICallingContext)
				DBSerializer.fromBlob(rs.getBlob(1));
			while (true)
			{
				try
				{
					_threadPool.enqueue(new JobChecker(
						jobID, resourceID, failedAttempts, 
						containerEndpoint, jobEndpoint,callingContext));
					break;
				}
				catch (InterruptedException ie)
				{
					Thread.interrupted();
				}
			}
		}
		catch (IOException ioe)
		{
			_logger.error("Unable to deserialize calling context.", ioe);
		}
		catch (ClassNotFoundException cnfe)
		{
			_logger.error("Unable to deserialize calling context.", cnfe);
		}
		catch (SQLException sqe)
		{
			_logger.error("Unable to query database.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	private void startJob(Connection conn, JobRequest request,
		ResourceSlot slot) throws SQLException
	{
		_logger.debug("Starting job " + request.getJobID() + " on resource " 
			+ slot.getResourceID());
		
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			stmt.addBatch("UPDATE queuejobinfo SET state = '" +
				QueueStates.STARTING.toString() + 
				"', starttime = CURRENT_TIMESTAMP WHERE jobid = " 
				+ request.getJobID());
			stmt.addBatch("INSERT INTO queueactivejobs " +
				"(queueid, jobid, resourceid, jobendpoint) VALUES " +
				"('" + _queueID + "', " + request.getJobID() + ", " +
				slot.getResourceID() + ", NULL)");
			stmt.executeBatch();
			conn.commit();
			
			while (true)
			{
				try
				{
					_threadPool.enqueue(new JobStarter(request, slot));
					break;
				}
				catch (InterruptedException ie)
				{
					Thread.interrupted();
				}
			}
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	private void updateRunningJob(JobRequest request, ResourceSlot slot,
		EndpointReferenceType activity) throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		PreparedStatement pStmt = null;
		
		try
		{
			conn = _connectionPool.acquire();
			stmt = conn.createStatement();
			
			if (activity == null)
			{
				// Fail resource
				stmt.addBatch("DELETE FROM queueactivejobs WHERE queueid = '" +
					_queueID + "' AND jobid = " + request.getJobID());
				stmt.addBatch("UPDATE queuejobinfo SET state = '" +
					QueueStates.REQUEUED.toString() + 
					"', starttime = NULL WHERE jobid = " +
					request.getJobID());
				stmt.addBatch(
					"UPDATE queuedynamicresourceinfo SET available = 0 " +
						"WHERE resourceid = " + slot.getResourceID());
				stmt.executeBatch();
			} else
			{
				pStmt = conn.prepareStatement(
					"UPDATE queueactivejobs SET jobendpoint = ? WHERE jobid = ?");
				pStmt.setBlob(1, EPRUtils.toBlob(activity));
				pStmt.setInt(2, request.getJobID());
				pStmt.executeUpdate();
				stmt.executeUpdate("UPDATE queuejobinfo SET state = '" +
					QueueStates.RUNNING.toString() + 
					"' WHERE jobid = " +
					request.getJobID());
			}
			
			conn.commit();
		}
		catch (ResourceException re)
		{
			_logger.error("Unable to serialize EPR.", re);
		}
		finally
		{
			StreamUtils.close(pStmt);
			StreamUtils.close(stmt);
			_connectionPool.release(conn);
		}
	}
	
	private void failJob(int jobID, int resourceID, int failedCount)
	{
		Connection conn = null;
		Statement stmt = null;
		
		try
		{
			conn = _connectionPool.acquire();
			stmt = conn.createStatement();
			failedCount++;
			
			stmt.addBatch("DELETE FROM queueactivejobs WHERE jobid = " + jobID);
			stmt.addBatch("UPDATE queuejobinfo SET state = '" +
				((failedCount >= _MAX_FAILED_ATTEMPTS) ?
					QueueStates.ERROR.toString() :
					QueueStates.REQUEUED.toString()) +
				"', starttime = NULL, failedattempts = " +
				failedCount + " WHERE jobid = " + jobID);
			stmt.executeBatch();
			conn.commit();
		}
		catch (SQLException sqe)
		{
			_logger.fatal("Unable to update database.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(conn);
		}
	}
	
	private void finishJob(int jobID, int resourceID)
	{
		Connection conn = null;
		Statement stmt = null;
		
		try
		{
			conn = _connectionPool.acquire();
			stmt = conn.createStatement();
			
			stmt.addBatch("DELETE FROM queueactivejobs WHERE jobid = " + jobID);
			stmt.addBatch("UPDATE queuejobinfo SET state = '" +
				QueueStates.FINISHED.toString() + 
				"', finishtime = CURRENT_TIMESTAMP WHERE jobid = " + jobID);
			stmt.executeBatch();
			conn.commit();
		}
		catch (SQLException sqe)
		{
			_logger.fatal("Unable to update database.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
			_connectionPool.release(conn);
		}
	}
	
	static final private String _FIND_STARTING_JOBS_STMT =
		"SELECT a.jobid FROM " +
			"(SELECT jobid FROM queuejobs WHERE queueid = ?) AS a INNER JOIN " +
			"(SELECT jobid FROM queuejobinfo WHERE state = 'STARTING') AS b ON " +
			"a.jobid = b.jobid";
	private void requeueStartingJobs()
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		Statement stmt2 = null;
		ResultSet rs = null;
		
		try
		{
			conn = _connectionPool.acquire();
			stmt = conn.prepareStatement(_FIND_STARTING_JOBS_STMT);
			stmt.setString(1, _queueID);
			rs = stmt.executeQuery();
		
			stmt2 = conn.createStatement();
	
			while (rs.next())
			{
				int jobid = rs.getInt(1);
				
				stmt2.addBatch(
					"UPDATE queuejobinfo SET state = 'REQUEUED' WHERE jobid = " 
						+ jobid);
				stmt2.addBatch(
					"DELETE FROM queueactivejobs WHERE jobid = " + jobid);
			}
			
			stmt2.executeBatch();
			
			conn.commit();
			
			jobSchedulingOpportunity();
		}
		catch (SQLException sqe)
		{
			_logger.error("Unable to read database.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt2);
			StreamUtils.close(stmt);
			_connectionPool.release(conn);
		}
	}
	
	private class JobStarter implements Runnable
	{
		private JobRequest _request;
		private ResourceSlot _slot;
		
		public JobStarter(JobRequest request, ResourceSlot slot)
		{
			_request = request;
			_slot = slot;
		}
		
		public void run()
		{
			BESPortType bes = null;
			EndpointReferenceType job = null;
			
			try
			{
				try
				{
					bes = ClientUtils.createProxy(BESPortType.class, 
						_slot.getResourceEndpoint(), _request.getCallingContext());
					job = bes.createActivity(
						new CreateActivityType(
							new ActivityDocumentType(
								_request.getJSDL(), null))).getActivityIdentifier();
					updateRunningJob(_request, _slot, job);
				}
				catch (Exception e)
				{
					_logger.error("Unable to start job on resource " 
						+ _slot.getResourceID());

					updateRunningJob(_request, _slot, null);
				}
			}
			catch (SQLException sqe)
			{
				_logger.fatal("Unable to update running job information.", sqe);
				if (bes != null && job != null)
				{
					try
					{
						bes.terminateActivities(new EndpointReferenceType[] { job });
					}
					catch (Throwable t)
					{
						_logger.fatal("Unable to kill bes job.  Leaking it...", t);
					}
				}
			}
		}
	}
	
	private class JobChecker implements Runnable
	{
		private int _jobID;
		private int _resourceID;
		private int _failedCount;
		private EndpointReferenceType _containerEndpoint;
		private EndpointReferenceType _activityEndpoint;
		private ICallingContext _callingContext;
		
		public JobChecker(int jobID, int resourceID, int failedCount,
			EndpointReferenceType containerEndpoint,
			EndpointReferenceType activityEndpoint,
			ICallingContext callingContext)
		{
			_jobID = jobID;
			_resourceID = resourceID;
			_containerEndpoint = containerEndpoint;
			_activityEndpoint = activityEndpoint;
			_callingContext = callingContext;
			_failedCount = failedCount;
		}
		
		public void run()
		{
			BESPortType bes = null;
			
			try
			{
				bes = ClientUtils.createProxy(BESPortType.class, _containerEndpoint,
					_callingContext);
				GetActivityStatusResponseType []statuses = 
					bes.getActivityStatuses(
						new EndpointReferenceType[] { _activityEndpoint });
				
				ActivityStatusType status = statuses[0].getActivityStatus();
				if ((status == null) || (status.get_any() == null) || (status.get_any().length == 0)) {
					// no status
					return;
				}
				
				ActivityState state = ActivityState.fromActivityStatus(status);
				
				if (state.isTerminalState())
				{
					if (state.isInState(ActivityState.FAILED))
					{
						// fail job
						failJob(_jobID, _resourceID, _failedCount);
					} else if (state.isInState(ActivityState.CANCELLED) ||
						state.isInState(ActivityState.FINISHED))
					{
						// finish job
						finishJob(_jobID, _resourceID);
					} else
					{
						return;
					}
					
					bes.terminateActivities(
						new EndpointReferenceType[] { _activityEndpoint });
					jobSchedulingOpportunity();
				}
			}
			catch (ConfigurationException ce)
			{
				_logger.fatal("Unable to create client proxy for bes.", ce);
			}
			catch (RemoteException re)
			{
				_logger.error("Error getting job status...nothing to do but wait.", re);
			}
			finally
			{
			}
		}
	}
	
	static private class SchedulingMarker
	{
		volatile private boolean _opportunity = true;
		
		public boolean haveOpportunity()
		{
			return _opportunity;
		}
		
		public void haveOpportunity(boolean opportunity)
		{
			_opportunity = opportunity;
		}
	}
}
