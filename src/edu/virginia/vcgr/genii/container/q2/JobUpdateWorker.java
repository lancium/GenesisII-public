package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.BESPortType;
import org.ggf.bes.factory.GetActivityStatusResponseType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class JobUpdateWorker implements Runnable
{
	static private Log _logger = LogFactory.getLog(JobUpdateWorker.class);
	
	private IBESPortTypeResolver _clientStubResolver;
	private JobCommunicationInfo _jobInfo;
	private JobManager _jobManager;
	private DatabaseConnectionPool _connectionPool;
	private IJobEndpointResolver _jobEndpointResolver;
	
	public JobUpdateWorker(JobManager jobManager,
		IBESPortTypeResolver clientStubResolver,
		IJobEndpointResolver jobEndpointResolver,
		DatabaseConnectionPool connectionPool,
		JobCommunicationInfo jobInfo)
	{
		_jobManager = jobManager;
		_clientStubResolver = clientStubResolver;
		_jobInfo = jobInfo;
		_connectionPool = connectionPool;
		_jobEndpointResolver = jobEndpointResolver;
	}
	
	public void run()
	{
		Connection connection = null;
		
		try
		{
			_logger.debug("Checking status of job " + _jobInfo.getJobID());
			
			connection = _connectionPool.acquire();
			EndpointReferenceType jobEndpoint = _jobEndpointResolver.getJobEndpoint(
				connection, _jobInfo.getJobID());
			BESPortType clientStub = _clientStubResolver.createClientStub(
				connection, _jobInfo.getBESID());
			GetActivityStatusResponseType []activityStatuses 
				= clientStub.getActivityStatuses(
					new EndpointReferenceType[] { jobEndpoint });
			if (activityStatuses == null || activityStatuses.length != 1)
			{
				_logger.error("Unable to get activity status for job " 
					+ _jobInfo.getJobID());
			} else
			{
				ActivityState state = ActivityState.fromActivityStatus(
					activityStatuses[0].getActivityStatus());
				if (state.isInState(ActivityState.FAILED))
				{
					_jobManager.failJob(connection, _jobInfo.getJobID(), true);
				} else if (state.isInState(ActivityState.CANCELLED))
				{
					_jobManager.finishJob(connection, _jobInfo.getJobID());
				} else if (state.isInState(ActivityState.FINISHED))
				{
					_jobManager.finishJob(connection, _jobInfo.getJobID());
				}
			}
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to update job status for job " 
				+ _jobInfo.getJobID(), cause);
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
}
