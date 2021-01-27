package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ThawActivitiesType;
import org.ggf.bes.factory.ThawActivityResponseType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;

//LAK (08 July 2020): Created to mimic the format of JobUpdateWorker for adding Thaw outcalls from the QueueManager GUI
public class JobThawWorker implements OutcallHandler {

	static private Log _logger = LogFactory.getLog(JobThawWorker.class);

	private IBESPortTypeResolver _clientStubResolver;
	private IJobEndpointResolver _jobEndpointResolver;
	private ServerDatabaseConnectionPool _connectionPool;
	private JobData _data;
	private QueueDatabase _queueDatabase;
	
	public JobThawWorker(IBESPortTypeResolver clientStubResolver, IJobEndpointResolver jobEndpointResolver, ServerDatabaseConnectionPool connectionPool, JobData data, QueueDatabase queueDatabase)
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
					_logger.debug(String.format("Making grid outcall to thaw job %s", _data));
				
				ThawActivityResponseType[] thawResponses;
				/* call the BES container to start persisting the job. */
				AddressingParameters aps = new AddressingParameters(jobEndpoint.getReferenceParameters());
				String epi = aps.getResourceKey();
				thawResponses = clientStub.thawActivities(new ThawActivitiesType(new String[] { epi }, null)).getResponse();
				
				for(ThawActivityResponseType rRes : thawResponses)
				{
					if(rRes.isThawed() == false)
					{
						_logger.error(String.format("Request to thaw job responded with a failure: %s", _data));
					}
					else
					{
						_queueDatabase.markThaw(connection, _data.getJobID());
						connection.commit();
						_data.setJobState(QueueStates.RUNNING);
					}
				}
			}
			catch (Throwable cause) 
			{
				if (_logger.isErrorEnabled())
					_logger.error(String.format("Failed to call thawActivities with exception %s", cause.toString()));
			}
		}
	}

	@Override
	public boolean equals(OutcallHandler other)
	{
		if (other instanceof JobThawWorker)
			return (_data.getJobID() == ((JobThawWorker)other)._data.getJobID());

		return false;
	}
}
