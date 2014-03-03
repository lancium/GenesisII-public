package edu.virginia.vcgr.genii.client.utils.units;

import java.util.Calendar;

/**
 * A simple class to store a duration. Durations are nothing more then longs
 * indicating the number of milliseconds in the duration. What makes this class
 * special is it's ability to parse duration descriptions from strings.
 * 
 * @author mmm2a
 */
public class Duration extends UnitableValue<DurationUnits> {
	static final long serialVersionUID = 0L;

	static public final long MILLISECONDS_PER_YEAR = DurationUnits.Years
			.multiplier();
	static public final long MILLISECONDS_PER_MONTH = DurationUnits.Months
			.multiplier();
	static public final long MILLISECONDS_PER_WEEK = DurationUnits.Weeks
			.multiplier();
	static public final long MILLISECONDS_PER_DAY = DurationUnits.Days
			.multiplier();
	static public final long MILLISECONDS_PER_HOUR = DurationUnits.Hours
			.multiplier();
	static public final long MILLISECONDS_PER_MINUTE = DurationUnits.Minutes
			.multiplier();
	static public final long MILLISECONDS_PER_SECOND = DurationUnits.Seconds
			.multiplier();

	@Override
	protected DurationUnits defaultUnits() {
		return DurationUnits.Milliseconds;
	}

	@Override
	protected DurationUnits parseUnits(String textRepresentation) {
		return DurationUnits.parse(textRepresentation);
	}

	@Override
	public double as(DurationUnits targetUnits) {
		return targetUnits.convert(value(), units());
	}

	public Duration() {
		super();
	}

	public Duration(double value, DurationUnits units) {
		super(value, units);
	}

	public Duration(double value) {
		super(value);
	}

	public Duration(String textRepresentation) {
		super(textRepresentation);
	}

	final public Calendar getTime() {
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(ret.getTimeInMillis()
				+ (long) as(DurationUnits.Milliseconds));
		return ret;
	}
}
