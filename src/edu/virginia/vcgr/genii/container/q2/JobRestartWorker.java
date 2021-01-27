package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.GetStatePathResponseType;
import org.ggf.bes.factory.GetStatePathsType;
import org.ggf.bes.factory.PersistActivityResponseType;
import org.ggf.bes.factory.RestartActivitiesType;
import org.ggf.bes.factory.RestartActivityResponseType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;

//LAK (08 July 2020): Created to mimic the format of JobUpdateWorker for adding Restart outcalls from the QueueManager GUI
public class JobRestartWorker implements OutcallHandler {

	static private Log _logger = LogFactory.getLog(JobRestartWorker.class);

	private IBESPortTypeResolver _clientStubResolver;
	private IJobEndpointResolver _jobEndpointResolver;
	private ServerDatabaseConnectionPool _connectionPool;
	private JobData _data;
	private QueueDatabase _queueDatabase;
	
	public JobRestartWorker(IBESPortTypeResolver clientStubResolver, IJobEndpointResolver jobEndpointResolver, ServerDatabaseConnectionPool connectionPool, JobData data, QueueDatabase queueDatabase)
	{
		_clientStubResolver = clientStubResolver;
		_jobEndpointResolver = jobEndpointResolver;
		_connectionPool = connectionPool;
		_data = data;
		_queueDatabase = queueDatabase;
	}
	
	@Override
	public void run() {
		Connection connection = null;
		long besID = -1L;

		synchronized (_data) {
			try {
				connection = _connectionPool.acquire(true);
				
				EndpointReferenceType jobEndpoint = null;
				try {
					jobEndpoint = _jobEndpointResolver.getJobEndpoint(connection, _data.getJobID());
				} catch (Throwable cause) {
					String message = String.format("Failure to get job endpoint on job %s with connection=%s jobId=%s", _data, connection,
						_data.getJobID());
					if(_logger.isErrorEnabled())
						_logger.error(message);
					throw cause;
				}
				
				besID = _data.getBESID().longValue();
				GeniiBESPortType clientStub = _clientStubResolver.createClientStub(connection, besID);
				ClientUtils.setTimeout(clientStub, 30 * 1000); //Changed to 30 seconds from 120 by ASG 2017-08-01

				if (_logger.isDebugEnabled())
					_logger.debug(String.format("Making grid outcall to restart job %s", _data));
				
				GetStatePathResponseType[] getStatePathResponses;
				
				// call the BES container to get the location the job's persisted data is stored
				AddressingParameters aps = new AddressingParameters(jobEndpoint.getReferenceParameters());
				String epi = aps.getResourceKey();
				getStatePathResponses = clientStub.getStatePaths(new GetStatePathsType(new String[]{epi}, null)).getResponse();
				
				if(getStatePathResponses.length != 1)
				{
					if(_logger.isErrorEnabled())
						_logger.error(String.format("GetStatePath returned an invalid number of responses for JobRestartWorker: %s", _data));
				}
				
				GetStatePathResponseType stateRes = getStatePathResponses[0];
				
				/* call the BES container to start restarting the job. */
				RestartActivityResponseType[] restartResponses = clientStub.restartActivities(new RestartActivitiesType(new String[] {stateRes.getPath()}, null)).getResponse();
				
				for(RestartActivityResponseType pRes : restartResponses)
				{
					if(pRes.isRestarted() == false)
					{
						if(_logger.isErrorEnabled())
							_logger.error(String.format("Request to restart job responded with a failure: %s", _data));
					}
					else
					{
						_queueDatabase.markRunning(connection, _data.getJobID(), _data.getJobEPR());
						connection.commit();
						_data.setJobState(QueueStates.RUNNING);
					}
				}
			}
			catch (Throwable cause) 
			{
				if(_logger.isErrorEnabled())
					_logger.error(String.format("Failed to call restartActivities with exception %s", cause.toString()));
			}
		}
	}

	@Override
	public boolean equals(OutcallHandler other)
	{
		if (other instanceof JobRestartWorker)
			return (_data.getJobID() == ((JobRestartWorker)other)._data.getJobID());

		return false;
	}
}
