package edu.virginia.vcgr.genii.container.cservices.percall;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PlannedScheduler implements AttemptScheduler
{
	static final long serialVersionUID = 0L;

	private List<Long> _schedule;

	static private Log _logger = LogFactory.getLog(PlannedScheduler.class);

	private void addToSchedule(Long value)
	{
		if (value <= 0L)
			throw new IllegalArgumentException("Schedule periods cannot be less than 0.");

		_schedule.add(value);
	}

	public PlannedScheduler(long... schedule)
	{
		_schedule = new Vector<Long>(schedule.length);
		for (long value : schedule)
			addToSchedule(new Long(value));
	}

	public PlannedScheduler(TimeUnit units, long... schedule)
	{
		_schedule = new Vector<Long>(schedule.length);
		for (long value : schedule)
			addToSchedule(new Long(TimeUnit.MILLISECONDS.convert(value, units)));
	}

	/*
	 * public PlannedScheduler(Pair<Long, TimeUnit>...schedule) { _schedule = new
	 * Vector<Long>(schedule.length); for (Pair<Long, TimeUnit> value : schedule) addToSchedule(new
	 * Long( TimeUnit.MILLISECONDS.convert(value.first(), value.second()))); }
	 */

	@Override
	final public Calendar nextAttempt(Calendar now, int numFailedAttempts)
	{
		if (numFailedAttempts > _schedule.size()) {
			if (_logger.isDebugEnabled())
				_logger.debug("PlanSched: scheduler says item failed too many times.");
			return null;
		}

		Calendar then = Calendar.getInstance();
		then.setTimeInMillis(now.getTimeInMillis() + _schedule.get(numFailedAttempts - 1));

		if (_logger.isDebugEnabled())
			_logger.debug("PlanSched: scheduler says item must wait until " + then);
		return then;
	}
}
