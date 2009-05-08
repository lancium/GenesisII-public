package edu.virginia.vcgr.genii.client.postlog;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.client.security.credentials.identity.Identity;

public class JobEvent implements PostEvent
{
	static public final String EVENT_TYPE_KEY = "eventtype";
	static public final String TIMESTAMP_KEY = "eventtime";
	static public final String EPI_KEY = "epi";
	static public final String JOBID_KEY = "jobid";
	static public final String USER_IDENTITY_KEY = "identity";
	static public final String HOSTNAME_KEY = "hostname";
	
	static private String _hostName;
	
	static
	{
		try
		{
			_hostName = Hostname.getMostGlobal().getCanonicalHostName();
		}
		catch (Throwable t)
		{
			_hostName = "<unknown>";
		}
	}
	
	private JobEventType _eventType;
	private String _user;
	private Calendar _timestamp;
	private String _epi;
	private String _jobid;
	
	static private String getCallerIdentities(ICallingContext ctxt)
	{
		StringBuilder builder = new StringBuilder();
		if (ctxt == null)
			return "<unknown>";
		
		boolean seenFirst = false;
		try
		{
			Collection<Identity> ids = SecurityUtils.getCallerIdentities(ctxt);
			if (ids != null)
			{
				for (Identity id : ids)
				{
					if (seenFirst)
						builder.append("|");
					seenFirst = true;
					
					builder.append(id.toString());
				}
			}
		}
		catch (Throwable cause)
		{
			builder.append("<unknown>");
		}
		
		return builder.toString();
	}
	
	static private String extractEPI(EndpointReferenceType epr)
	{
		if (epr == null)
			return "<unknown>";
		WSName name = new WSName(epr);
		if (name.isValidWSName())
			return name.getEndpointIdentifier().toString();
		
		return "<unknown>";
	}
	
	private JobEvent(JobEventType eventType,
		ICallingContext user, Calendar timestamp, String epi, String jobid)
	{
		_eventType = eventType;
		_user = getCallerIdentities(user);
		_timestamp = timestamp;
		_epi = epi;
		_jobid = jobid;
	}
	
	@Override
	public Map<String, String> content()
	{
		Map<String, String> ret = new HashMap<String, String>();
		
		ret.put(EVENT_TYPE_KEY, _eventType.name());
		ret.put(TIMESTAMP_KEY, 
			Long.toString(_timestamp.getTimeInMillis() / 1000L));
		ret.put(EPI_KEY, _epi);
		ret.put(JOBID_KEY, _jobid);
		ret.put(USER_IDENTITY_KEY, _user);
		ret.put(HOSTNAME_KEY, _hostName);
		
		return ret;
	}
	
	static public PostEvent jobSubmitted(ICallingContext user, String jobid)
	{
		return new JobEvent(JobEventType.JOB_SUBMITTED, user, 
			Calendar.getInstance(), null, jobid);
	}
	
	static public PostEvent jobLaunched(ICallingContext user, 
		EndpointReferenceType epi, String jobid)
	{
		return new JobEvent(JobEventType.JOB_LAUNCHED, user,
			Calendar.getInstance(), extractEPI(epi), jobid);
	}
	
	static public PostEvent jobRequeued(ICallingContext user, String jobid)
	{
		return new JobEvent(JobEventType.JOB_REQUEUED,
			user, Calendar.getInstance(), null, jobid);
	}
	
	static public PostEvent jobFailed(ICallingContext user, String jobid)
	{
		return new JobEvent(JobEventType.JOB_FAILED, user,
			Calendar.getInstance(), null, jobid);
	}
	
	static public PostEvent jobFinished(ICallingContext user, String jobid)
	{
		return new JobEvent(JobEventType.JOB_FINISHED, user,
			Calendar.getInstance(), null, jobid);
	}
	
	static public PostEvent jobKilled(ICallingContext user, String jobid)
	{
		return new JobEvent(JobEventType.JOB_KILLED, user,
			Calendar.getInstance(), null, jobid);
	}
	
	static public PostEvent jobCompleted(ICallingContext user, String jobid)
	{
		return new JobEvent(JobEventType.JOB_COMPLETED, user,
			Calendar.getInstance(), null, jobid);
	}
	
	static public PostEvent activityCreated(ICallingContext user, 
		String epi)
	{
		return new JobEvent(JobEventType.ACTIVITY_CREATED, user,
			Calendar.getInstance(), epi, null);
	}
	
	static public PostEvent activityFailed(ICallingContext user, 
		String epi)
	{
		return new JobEvent(JobEventType.ACTIVITY_FAILED, user,
			Calendar.getInstance(), epi, null);
	}
	
	static public PostEvent activityFinished(ICallingContext user,  
		String epi)
	{
		return new JobEvent(JobEventType.ACTIVITY_FINISHED, user,
			Calendar.getInstance(), epi, null);
	}
	
	static public PostEvent activityTerminated(ICallingContext user, 
		String epi)
	{
		return new JobEvent(JobEventType.ACTIVITY_TERMINATED, user,
			Calendar.getInstance(), epi, null);
	}
	
	static public PostEvent activityDeleted(ICallingContext user, 
		String epi)
	{
		return new JobEvent(JobEventType.ACTIVITY_DELETED, user,
			Calendar.getInstance(), epi, null);
	}	
}