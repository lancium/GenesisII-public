package edu.virginia.vcgr.genii.container.q2;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.bes.factory.ActivityStatusType;
import org.ggf.bes.factory.GetActivityStatusResponseType;
import org.ggf.bes.factory.GetActivityStatusesType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.soap.envelope.Fault;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESFaultManager;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
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
		Connection connection = null;
		long besID = -1L;
		HistoryContext history = _data.history(HistoryEventCategory.Checking);
		
		try
		{
			
			ActivityState oldState = null;
			
			_logger.debug("Checking status of job " + _data);
	
			/* Get a connection from the connection pool and then ask the 
			 * resolvers to get the memory "large" information from the
			 * database.
			 */
			connection = _connectionPool.acquire(false);
			EndpointReferenceType jobEndpoint = null;
			try {
				jobEndpoint = _jobEndpointResolver.getJobEndpoint(connection, _jobInfo.getJobID());
			} catch (Throwable cause) {
				String message = String.format("Failure to get job endpoint on job %s with connection=%s jobId=%s", _data, connection, _jobInfo.getJobID()); 
				_logger.error(message);
				history.error(message);
                                throw cause;
			}
			besID = _jobInfo.getBESID();
			GeniiBESPortType clientStub = _clientStubResolver.createClientStub(
				connection, besID);
			try { connection.commit(); } catch (Throwable c) {}
			ClientUtils.setTimeout(clientStub, 120 * 1000);
			
			if (jobEndpoint == null)
			{
				history.createDebugWriter("Job No Longer Running").format(
					"Job is no longer marked as running " +
					"(anticipated asynchronous behavior).").close();
				
				// Another thread has removed this from the database.
				_logger.debug(String.format(
					"Asked to check status on job %s which is " +
					"no longer running according to the database.",
					_data));
				_jobManager.failJob(connection, _jobInfo.getJobID(), false, false, false);
				return;
			}
			
			String oldAction = _data.setJobAction("Checking");
			if (oldAction != null)
			{
				history.debug("Job Busy Doing %s", oldAction);
				_logger.error(String.format(
					"Attempting to check job status for %s, found that " +
					"we are in the process of doing action:  " + oldAction,
					_data));
				return;
			}
			
			boolean wasSecurityException = false;
			GetActivityStatusResponseType []activityStatuses;
			try
			{
				_logger.debug(String.format(
					"Making grid outcall to check status of job %s", _data));
				/* call the BES container to get the activity's status. */
				activityStatuses 
					= clientStub.getActivityStatuses(new GetActivityStatusesType(
						new EndpointReferenceType[] { jobEndpoint }, null)).getResponse();
				_jobManager.resetJobCommunicationAttempts(connection, _jobInfo.getJobID());
				connection.commit();
			}
			catch (GenesisIISecurityException gse)
			{
				history.createErrorWriter(gse, "Job Status Checked Failed").format(
					"Security exception while checking job status --" +
					" marking jobs as failed.").close();
				
				_logger.debug(String.format(
					"There was a security exception checking on status of job %s.",
					_data), gse);
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
				history.createWarnWriter("Job Status Check Failed").format(
					"BES didn't return the expected information for the job.").close();
				_logger.error("Unable to get activity status for job " 
					+ _data);
			} else
			{
				_logger.debug(String.format(
					"Successfully got status of job %s.", _data));
				List<String> faults = null;
				
					
							
				oldState = new ActivityState(_data.getBESActivityStatus());
						
				
				_data.setBESActivityStatus(
					activityStatuses[0].getActivityStatus());
				try
				{
					if (activityStatuses[0] != null)
					{
						Fault fault = activityStatuses[0].getFault();
						if (fault != null)
						{
							faults = BESFaultManager.getFaultDetail(
								fault);
							PrintWriter faultWriter = history.createErrorWriter(
									"Job Faulted").format(
										"Job threw fault:\n");
							try
							{
								ObjectSerializer.serialize(faultWriter, fault, 
									new QName("http://tempuri.org", "Fault"));
							}
							catch (Throwable cause)
							{
								faultWriter.format("\n\nUnable to show fault.");
							}
							finally
							{
								StreamUtils.close(faultWriter);
							}
						}
					}
				}
				catch (Throwable cause)
				{
					history.error(cause, "Unable to Get Job Fault");
					_logger.error(String.format(
						"Error trying to get fault detail for job %s.", _data), cause);
				}
				
/*				if (faults == null)
					faults = new Vector<String>(1);
				faults.add(0, String.format("Job ran on BES resource %s.",
					besName == null ? "<unknown>" : besName));
*/				
				if (faults != null && faults.size() > 0)
				{
					_jobManager.addJobErrorInformation(
						connection, _jobInfo.getJobID(), 
						_data.getRunAttempts(), faults);
				}
				
				if (wasSecurityException)
				{
					history.createDebugWriter("Certificate Expired").format(
						"It looks like the certificate for the job might have expired.");
					
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
				
				//Only add to log/history if status changed
				if (!oldState.equals(state)){
					_logger.debug(String.format(
							"Job %s has activity status %s.", _data, state));
					history.trace("Job Status on BES:  %s", state);
				}
				
				if (state.isFailedState())
				{
					/* If the job failed in the BES, fail it in the queue */
					_jobManager.failJob(connection, _jobInfo.getJobID(), 
						!state.isIgnoreable(), false, true);
				} else if (state.isCancelledState())
				{
					/* If the job was cancelled, then finish it here */
					_jobManager.failJob(connection, _jobInfo.getJobID(), 
							false, false, true);
				} else if (state.isFinishedState())
				{
					/* If the job finished on the bes, finish it here */
					_jobManager.finishJob(_jobInfo.getJobID());
				}
			}
		}
		catch (Throwable cause)
		{
			history.warn(cause, "Error Updating Job State");
			_logger.warn("Unable to update job status for job " 
				+ _data, cause);
			try
			{
				_jobManager.addJobCommunicationAttempt(connection,
					_data.getJobID(), _data.getBESID().longValue());
				connection.commit();
			}
			catch (Throwable cause2)
			{
				_logger.warn(String.format(
					"Unable to update database for failed job %s.", 
					_data), cause2);
			}
		}
		finally
		{
			_connectionPool.release(connection);
		}
	}
}
