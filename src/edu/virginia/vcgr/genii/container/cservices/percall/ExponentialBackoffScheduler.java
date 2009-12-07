package edu.virginia.vcgr.genii.container.cservices.percall;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ExponentialBackoffScheduler implements AttemptScheduler
{
	static final long serialVersionUID = 0L;
	
	static private Random TWITTER_GENERATOR = new Random();
	
	private Calendar _lifetime = null;
	private Integer _maxFailedAttempts = null;
	private long _backoffBase;
	private Long _backoffTwitterBase = null;
	
	public ExponentialBackoffScheduler(
		Calendar lifetime, Integer maxFailedAttempts,
		long backoffBase, TimeUnit backoffBaseUnits,
		Long backoffTwitterBase, TimeUnit backoffTwitterBaseUnits)
	{
		if (backoffBaseUnits == null)
			backoffBaseUnits = TimeUnit.MILLISECONDS;
		
		if (backoffTwitterBaseUnits == null)
			backoffTwitterBaseUnits = TimeUnit.MILLISECONDS;
		
		_lifetime = lifetime;
		_maxFailedAttempts = maxFailedAttempts;
		_backoffBase = backoffBaseUnits.convert(backoffBase,
			TimeUnit.MILLISECONDS);
		_backoffTwitterBase = (backoffTwitterBase == null) ? null :
			backoffTwitterBaseUnits.convert(backoffTwitterBase,
				TimeUnit.MILLISECONDS);
	}
	
	public ExponentialBackoffScheduler(
		long lifetime, TimeUnit lifetimeUnits, Integer maxFailedAttempts,
		long backoffBase, TimeUnit backoffBaseUnits,
		Long backoffTwitterBase, TimeUnit backoffTwitterBaseUnits)
	{
		if (backoffBaseUnits == null)
			backoffBaseUnits = TimeUnit.MILLISECONDS;
		
		if (backoffTwitterBaseUnits == null)
			backoffTwitterBaseUnits = TimeUnit.MILLISECONDS;
		
		if (lifetimeUnits == null)
			lifetimeUnits = TimeUnit.MILLISECONDS;
		
		_lifetime = Calendar.getInstance();
		_lifetime.setTimeInMillis(System.currentTimeMillis() +
			lifetimeUnits.convert(lifetime, TimeUnit.MILLISECONDS));
		
		_maxFailedAttempts = maxFailedAttempts;
		_backoffBase = backoffBaseUnits.convert(backoffBase,
			TimeUnit.MILLISECONDS);
		_backoffTwitterBase = (backoffTwitterBase == null) ? null :
			backoffTwitterBaseUnits.convert(backoffTwitterBase,
				TimeUnit.MILLISECONDS);
	}
	
	@Override
	public Calendar nextAttempt(Calendar now, int numFailedAttempts)
	{
		if (_lifetime != null && now.after(_lifetime))
			return null;
		if (_maxFailedAttempts != null && numFailedAttempts >= _maxFailedAttempts)
			return null;
		
		long delay = _backoffBase << numFailedAttempts;
		if (_backoffTwitterBase != null)
		{
			long twitterRange = _backoffTwitterBase << (numFailedAttempts + 1);
			long twitter = (TWITTER_GENERATOR.nextLong() & ~Long.MAX_VALUE)
				% twitterRange;
			delay += (twitter - (twitterRange >> 1));
		}
		
		if (delay < 0L)
			delay = 0L;
		
		Calendar next = Calendar.getInstance();
		next.setTimeInMillis(now.getTimeInMillis() + delay);
		return next;
	}
}