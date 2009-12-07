package edu.virginia.vcgr.genii.container.cservices.percall;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class PeriodicScheduler implements AttemptScheduler
{
	static final long serialVersionUID = 0L;
	
	private Calendar _lifetime;
	private Integer _maxFailedAttempts;
	private long _period;
	
	public PeriodicScheduler(Calendar lifetime, Integer maxFailedAttempts,
		long period, TimeUnit periodUnits)
	{
		if (periodUnits == null)
			periodUnits = TimeUnit.MILLISECONDS;
		
		_lifetime = lifetime;
		_maxFailedAttempts = maxFailedAttempts;
		_period = periodUnits.convert(period, TimeUnit.MILLISECONDS);
	}
	
	public PeriodicScheduler(long lifetime, TimeUnit lifetimeUnits,
		Integer maxFailedAttempts,
		long period, TimeUnit periodUnits)
	{
		if (lifetimeUnits == null)
			lifetimeUnits = TimeUnit.MILLISECONDS;
		
		if (periodUnits == null)
			periodUnits = TimeUnit.MILLISECONDS;
		
		_lifetime = Calendar.getInstance();
		_lifetime.setTimeInMillis(System.currentTimeMillis() +
			lifetimeUnits.convert(lifetime, TimeUnit.MILLISECONDS));
		
		_maxFailedAttempts = maxFailedAttempts;
		_period = periodUnits.convert(period, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public Calendar nextAttempt(Calendar now, int numFailedAttempts)
	{
		if (_lifetime != null && now.after(_lifetime))
			return null;
		if (_maxFailedAttempts != null && numFailedAttempts >= _maxFailedAttempts)
			return null;
		
		Calendar next = Calendar.getInstance();
		next.setTimeInMillis(System.currentTimeMillis() + _period);
		return next;
	}
}