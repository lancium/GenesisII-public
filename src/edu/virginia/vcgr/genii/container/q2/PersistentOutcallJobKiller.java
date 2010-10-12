package edu.virginia.vcgr.genii.container.q2;

import java.util.concurrent.TimeUnit;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryEventToken;
import edu.virginia.vcgr.genii.container.cservices.percall.AttemptScheduler;
import edu.virginia.vcgr.genii.container.cservices.percall.BESActivityTerminatorActor;
import edu.virginia.vcgr.genii.container.cservices.percall.ExponentialBackoffScheduler;
import edu.virginia.vcgr.genii.container.cservices.percall.PersistentOutcallContainerService;

class PersistentOutcallJobKiller
{
	static final private long LIFETIME_CAP = 30;
	static final private TimeUnit LIFETIME_CAP_UNITS = TimeUnit.DAYS;
	
	static final private int EXPONENT_ATTEMPT_CAP = 4;
	
	static final private long BACKOFF_BASE = 1;
	static final private TimeUnit BACKOFF_BASE_UNITS = TimeUnit.HOURS;
	
	static final private long BACKOFF_JITTER_BASE = 15;
	static final private TimeUnit BACKOFF_JITTER_BASE_UNITS = TimeUnit.MINUTES;
	
	static private AttemptScheduler SCHEDULER()
	{
		return new ExponentialBackoffScheduler(
			LIFETIME_CAP, LIFETIME_CAP_UNITS,
			null, EXPONENT_ATTEMPT_CAP,
			BACKOFF_BASE, BACKOFF_BASE_UNITS,
			BACKOFF_JITTER_BASE, BACKOFF_JITTER_BASE_UNITS);
	}
	
	static boolean killJob(String besName, EndpointReferenceType bes,
		String historyKey, HistoryEventToken historyToken,
		EndpointReferenceType activity, ICallingContext context)
	{
		return PersistentOutcallContainerService.schedulePersistentOutcall(
			new BESActivityTerminatorActor(
				historyKey, historyToken, besName, activity),
			SCHEDULER(), bes, context);
	}
}