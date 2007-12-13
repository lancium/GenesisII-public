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
	
	private BESPortType _clientStub;
	private JobCommunicationInfo _jobInfo;
	private JobManager _jobManager;
	private DatabaseConnectionPool _connectionPool;
	
	public JobUpdateWorker(JobManager jobManager,
		BESPortType clientStub, DatabaseConnectionPool connectionPool,
		JobCommunicationInfo jobInfo)
	{
		_jobManager = jobManager;
		_clientStub = clientStub;
		_jobInfo = jobInfo;
		_connectionPool = connectionPool;
	}
	
	public void run()
	{
		try
		{
			_logger.debug("Checking status of job " + _jobInfo.getJobID());
			
			GetActivityStatusResponseType []activityStatuses 
				= _clientStub.getActivityStatuses(
					new EndpointReferenceType[] { 
						_jobInfo.getJobEndpoint() } );
			if (activityStatuses == null || activityStatuses.length != 1)
			{
				_logger.error("Unable to get activity status for job " 
					+ _jobInfo.getJobID());
			} else
			{
				ActivityState state = ActivityState.fromActivityStatus(
					activityStatuses[0].getActivityStatus());
				Connection connection = null;
				
				try
				{
					if (state.isInState(ActivityState.FAILED))
					{
						connection = _connectionPool.acquire();
						_jobManager.failJob(connection, _jobInfo.getJobID(), true);
					} else if (state.isInState(ActivityState.CANCELLED))
					{
						connection = _connectionPool.acquire();
						_jobManager.finishJob(connection, _jobInfo.getJobID());
					} else if (state.isInState(ActivityState.FINISHED))
					{
						connection = _connectionPool.acquire();
						_jobManager.finishJob(connection, _jobInfo.getJobID());
					}
				}
				finally
				{
					_connectionPool.release(connection);
				}
			}
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to update job status for job " 
				+ _jobInfo.getJobID(), cause);
		}
	}
}
