package edu.virginia.vcgr.genii.container.cservices.percall;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PeriodicScheduler implements AttemptScheduler
{
	static final long serialVersionUID = 0L;
	
	private Calendar _lifetime;
	private Integer _maxFailedAttempts;
	private long _period;
	
	static private Log _logger = LogFactory.getLog(PeriodicScheduler.class);

	public PeriodicScheduler(Calendar lifetime, Integer maxFailedAttempts,
		long period, TimeUnit periodUnits)
	{	
		if (period < 0L)
			throw new IllegalArgumentException("Period cannot be negative.");
	
		if (periodUnits == null)
			periodUnits = TimeUnit.MILLISECONDS;
		
		_lifetime = lifetime;
		_maxFailedAttempts = maxFailedAttempts;
		_period = TimeUnit.MILLISECONDS.convert(period, periodUnits);
	}
	
	public PeriodicScheduler(long lifetime, TimeUnit lifetimeUnits,
		Integer maxFailedAttempts,
		long period, TimeUnit periodUnits)
	{
		if (period < 0L)
			throw new IllegalArgumentException("Period cannot be negative.");
		
		if (lifetimeUnits == null)
			lifetimeUnits = TimeUnit.MILLISECONDS;
		
		if (periodUnits == null)
			periodUnits = TimeUnit.MILLISECONDS;
		
		_lifetime = Calendar.getInstance();
		_lifetime.setTimeInMillis(System.currentTimeMillis() +
			TimeUnit.MILLISECONDS.convert(lifetime, lifetimeUnits));
		
		_maxFailedAttempts = maxFailedAttempts;
		_period = TimeUnit.MILLISECONDS.convert(period, periodUnits);
	}
	
	@Override
	final public Calendar nextAttempt(Calendar now, int numFailedAttempts)
	{
		if (_lifetime != null && now.after(_lifetime)) {
			_logger.debug("PerioSched: scheduler says item lifetime has passed.");
			return null;
		}
		if (_maxFailedAttempts != null && numFailedAttempts >= _maxFailedAttempts) {
			_logger.debug("PerioSched: scheduler says item failed too many times.");
			return null;
		}
		
		Calendar next = Calendar.getInstance();
		_logger.debug("PerioSched: scheduler says item must wait " + _period + " milliseconds.");
		next.setTimeInMillis(System.currentTimeMillis() + _period);
		return next;
	}
}
