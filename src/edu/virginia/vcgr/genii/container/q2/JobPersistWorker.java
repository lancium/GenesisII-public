package edu.virginia.vcgr.genii.container.q2;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.PersistActivitiesType;
import org.ggf.bes.factory.PersistActivityResponseType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;

//LAK (08 July 2020): Created to mimic the format of JobUpdateWorker for adding Persist outcalls from the QueueManager GUI
public class JobPersistWorker implements OutcallHandler {

	static private Log _logger = LogFactory.getLog(JobPersistWorker.class);

	private IBESPortTypeResolver _clientStubResolver;
	private IJobEndpointResolver _jobEndpointResolver;
	private ServerDatabaseConnectionPool _connectionPool;
	private JobData _data;
	
	public JobPersistWorker(IBESPortTypeResolver clientStubResolver, IJobEndpointResolver jobEndpointResolver, ServerDatabaseConnectionPool connectionPool, JobData data)
		{
			_clientStubResolver = clientStubResolver;
			_jobEndpointResolver = jobEndpointResolver;
			_connectionPool = connectionPool;
			_data = data;
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
					_logger.debug(String.format("Making grid outcall to persist job %s", _data));
				
				PersistActivityResponseType[] persistResponses;
				/* call the BES container to start persisting the job. */
				AddressingParameters aps = new AddressingParameters(jobEndpoint.getReferenceParameters());
				String epi = aps.getResourceKey();
				persistResponses = clientStub.persistActivities(new PersistActivitiesType(new String[]{epi}, false, null)).getResponse();
				
				for(PersistActivityResponseType pRes : persistResponses)
				{
					if(pRes.isPersisting() == false)
					{
						if(_logger.isErrorEnabled())
							_logger.error(String.format("Request to persist job responded with a failure: %s", _data));
					}
				}
			}
			catch (Throwable cause) 
			{
				if (_logger.isErrorEnabled())
					_logger.error(String.format("Failed to call persistActivities with exception %s", cause.toString()));
			}
		}
	}

	@Override
	public boolean equals(OutcallHandler other)
	{
		if (other instanceof JobPersistWorker)
			return (_data.getJobID() == ((JobPersistWorker)other)._data.getJobID());

		return false;
	}
}