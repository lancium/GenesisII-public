package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.bes.factory.ActivityStatusType;
import org.ggf.bes.factory.GetActivityStatusResponseType;
import org.ggf.bes.factory.GetActivityStatusesType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESFaultManager;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.gridlog.GridLogTarget;
import edu.virginia.vcgr.genii.client.postlog.JobEvent;
import edu.virginia.vcgr.genii.client.postlog.PostTargets;
import edu.virginia.vcgr.genii.container.cservices.gridlogger.GridLogDevice;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

/**
 * The asynchronous worker that actually makes the outcall to the bes container
 * to see if a job has finished or failed.
 * 
 * @author mmm2a
 */
public class JobUpdateWorker implements OutcallHandler
{
	static private Log _logger = LogFactory.getLog(JobUpdateWorker.class);
	static private GridLogDevice _jobLogger = new GridLogDevice(JobUpdateWorker.class);
	
	private IBESPortTypeResolver _clientStubResolver;
	private JobCommunicationInfo _jobInfo;
	private JobManager _jobManager;
	private DatabaseConnectionPool _connectionPool;
	private IJobEndpointResolver _jobEndpointResolver;
	private JobData _data;
	
	public JobUpdateWorker(JobManager jobManager,
		IBESPortTypeResolver clientStubResolver,
		IJobEndpointResolver jobEndpointResolver,
		DatabaseConnectionPool connectionPool,
		JobCommunicationInfo jobInfo, JobData data)
	{
		_jobManager = jobManager;
		_clientStubResolver = clientStubResolver;
		_jobInfo = jobInfo;
		_connectionPool = connectionPool;
		_jobEndpointResolver = jobEndpointResolver;
		_data = data;
	}
	
	public boolean equals(JobUpdateWorker other)
	{
		return (_data.getJobID() == other._data.getJobID());
	}
	
	@Override
	public boolean equals(OutcallHandler other)
	{
		if (other instanceof JobUpdateWorker)
			return equals((JobUpdateWorker)other);
		
		return false;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof JobUpdateWorker)
			return equals((JobUpdateWorker)other);
		
		return false;
	}
	
	public void run()
	{
		Collection<GridLogTarget> logTargets = _data.gridLogTargets();
		
		Connection connection = null;
		long besID = -1L;
		
		try
		{
			_jobLogger.log(logTargets, "Checking status of the job.");
			_logger.debug("Checking status of job " + _jobInfo.getJobID());
	
			/* Get a connection from the connection pool and then ask the 
			 * resolvers to get the memory "large" information from the
			 * database.
			 */
			connection = _connectionPool.acquire(false);
			EndpointReferenceType jobEndpoint = _jobEndpointResolver.getJobEndpoint(
				connection, _jobInfo.getJobID());
			besID = _jobInfo.getBESID();
			String besName = _clientStubResolver.getBESName(besID);
			GeniiBESPortType clientStub = _clientStubResolver.createClientStub(
				connection, besID);
			try { connection.commit(); } catch (Throwable c) {}
			ClientUtils.setTimeout(clientStub, 120 * 1000);
			
			if (jobEndpoint == null)
			{
				// Another thread has removed this from the database.
				_logger.debug(
					"Asked to check status on a job which is " +
					"no longer running according to the database.");
				_jobManager.failJob(connection, _jobInfo.getJobID(), false, false, false);
				return;
			}
			
			String oldAction = _data.setJobAction("Checking");
			if (oldAction != null)
			{
				_logger.error("Attempting to check job status, found that " +
					"we are in the process of doing action:  " + oldAction);
				return;
			}
			
			boolean wasSecurityException = false;
			GetActivityStatusResponseType []activityStatuses;
			try
			{
				_jobLogger.log(logTargets, "Making grid outcall to get activity status.");
				/* call the BES container to get the activity's status. */
				activityStatuses 
					= clientStub.getActivityStatuses(new GetActivityStatusesType(
						new EndpointReferenceType[] { jobEndpoint }, null)).getResponse();
				_jobManager.resetJobCommunicationAttempts(connection, _jobInfo.getJobID());
				connection.commit();
			}
			catch (GenesisIISecurityException gse)
			{
				wasSecurityException = true;
				activityStatuses = new GetActivityStatusResponseType[] {
					new GetActivityStatusResponseType(
						jobEndpoint, new ActivityStatusType(null, ActivityStateEnumeration.Failed),
						null, null)
				};
			}
			finally
			{
				_data.clearJobAction();
			}
			
			/* If we didn't get one back, then there was 
			 * a weird internal error. */
			if (activityStatuses == null || activityStatuses.length != 1)
			{
				_jobLogger.log(logTargets, "Unable to get activity status for the job.");
				_logger.error("Unable to get activity status for job " 
					+ _jobInfo.getJobID());
			} else
			{
				List<String> faults = null;
				
				try
				{ 
					faults = BESFaultManager.getFaultDetail(
						activityStatuses[0].getFault());
				}
				catch (Throwable cause)
				{
					_logger.error("Error trying to get fault detail.", cause);
				}
				
				if (faults == null)
					faults = new Vector<String>(1);
				faults.add(0, String.format("Job ran on BES resource %s.",
					besName == null ? "<unknown>" : besName));
				
				if (faults != null && faults.size() > 0)
				{
					_jobManager.addJobErrorInformation(
						connection, _jobInfo.getJobID(), 
						_data.getRunAttempts(), faults);
				}
				
				if (wasSecurityException)
				{
					Collection<String> messages = new Vector<String>();
					messages.add("The certificates for this job have expired.");
					_jobManager.addJobErrorInformation(connection, _jobInfo.getJobID(), _data.getRunAttempts(), messages);
				}
				
				/* We have it's status, convert it to a more reasonable data
				 * type (not the auto generated from WSDL one which is
				 * worthless).
				 */
				ActivityState state = new ActivityState(
					activityStatuses[0].getActivityStatus());
				_jobLogger.log(logTargets, String.format(
					"The activity's status is \"%s\".", state));
				if (state.isFailedState())
				{
					/* If the job failed in the BES, fail it in the queue */
					if (!_jobManager.failJob(connection, _jobInfo.getJobID(), 
						!state.isIgnoreable(), false, true))
						PostTargets.poster().post(
							JobEvent.jobFailed(null, 
								Long.toString(_jobInfo.getJobID())));
				} else if (state.isCancelledState())
				{
					/* If the job was cancelled, then finish it here */
					_jobManager.failJob(connection, _jobInfo.getJobID(), 
							false, false, true);
					PostTargets.poster().post(JobEvent.jobKilled(null,
						Long.toString(_jobInfo.getJobID())));
				} else if (state.isFinishedState())
				{
					/* If the job finished on the bes, finish it here */
					_jobManager.finishJob(_jobInfo.getJobID());
					PostTargets.poster().post(JobEvent.jobFinished(null,
						Long.toString(_jobInfo.getJobID())));
				}
			}
		}
		catch (Throwable cause)
		{
			_jobLogger.log(logTargets, "Unable to update job status for job.", cause);
			_logger.warn("Unable to update job status for job " 
				+ _jobInfo.getJobID(), cause);
			try
			{
				_jobManager.addJobCommunicationAttempt(connection,
					_data.getJobID(), _data.getBESID().longValue(),
					logTargets);
				connection.commit();
			}
			catch (Throwable cause2)
			{
				_logger.warn(String.format(
					"Unable to update database for failed job %d.", 
					_jobInfo.getJobID()), cause2);
			}
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
}
