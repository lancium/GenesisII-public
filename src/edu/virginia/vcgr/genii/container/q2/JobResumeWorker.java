package edu.virginia.vcgr.genii.container.q2;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.bes.factory.ResumeActivitiesType;
import org.ggf.bes.factory.ResumeActivityResponseType;
import org.ggf.bes.factory.ResumeActivitiesResponseType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.soap.envelope.Fault;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESFaultManager;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;

//LAK (08 July 2020): Created to mimic the format of JobUpdateWorker for adding Resume outcalls from the QueueManager GUI
public class JobResumeWorker implements OutcallHandler {

	static private Log _logger = LogFactory.getLog(JobResumeWorker.class);

	private IBESPortTypeResolver _clientStubResolver;
	private IJobEndpointResolver _jobEndpointResolver;
	private ServerDatabaseConnectionPool _connectionPool;
	private JobData _data;
	
	public JobResumeWorker(IBESPortTypeResolver clientStubResolver, IJobEndpointResolver jobEndpointResolver, ServerDatabaseConnectionPool connectionPool, JobData data)
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
					_logger.debug(String.format("Making grid outcall to resume job %s", _data));
				
				ResumeActivityResponseType[] resumeResponses;
				/* call the BES container to start persisting the job. */
				resumeResponses = clientStub.resumeActivities(new ResumeActivitiesType(new String[] { _data.getJobTicket() }, null)).getResponse();
				
				for(ResumeActivityResponseType rRes : resumeResponses)
				{
					if(rRes.isResumed() == false)
					{
						_logger.error(String.format("Request to resume job responded with a failure: %s", _data));
					}
				}
			}
			catch (Throwable cause) 
			{
				if (_logger.isErrorEnabled())
					_logger.error(String.format("Failed to call resumeActivities with exception %s", cause.toString()));
			}
		}
	}

	@Override
	public boolean equals(OutcallHandler other)
	{
		if (other instanceof JobResumeWorker)
			return (_data.getJobID() == ((JobResumeWorker)other)._data.getJobID());

		return false;
	}
}
