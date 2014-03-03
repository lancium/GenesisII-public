package edu.virginia.vcgr.genii.client.utils.units;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A simple class to store a duration. Durations are nothing more then longs
 * indicating the number of milliseconds in the duration. What makes this class
 * special is it's ability to parse duration descriptions from strings.
 * 
 * @author mmm2a
 */
@XmlJavaTypeAdapter(DurationXmlAdapter.class)
public class AxisDuration extends Duration {
	static final long serialVersionUID = 0L;

	public AxisDuration() {
		super();
	}

	public AxisDuration(Duration toCopy) {
		super(toCopy.value(), toCopy.units());
	}

	public static org.apache.axis.types.Duration toApacheDuration(
			Duration toConvert) {
		int years;
		int days;
		int hours;
		int minutes;
		int seconds;

		long millis = (long) toConvert.units()
				.toMilliseconds(toConvert.value());
		years = (int) (millis / Duration.MILLISECONDS_PER_YEAR);
		millis %= Duration.MILLISECONDS_PER_YEAR;

		days = (int) (millis / Duration.MILLISECONDS_PER_DAY);
		millis %= Duration.MILLISECONDS_PER_DAY;

		hours = (int) (millis / Duration.MILLISECONDS_PER_HOUR);
		millis %= Duration.MILLISECONDS_PER_HOUR;

		minutes = (int) (millis / Duration.MILLISECONDS_PER_MINUTE);
		millis %= Duration.MILLISECONDS_PER_MINUTE;

		seconds = (int) (millis / Duration.MILLISECONDS_PER_SECOND);

		return new org.apache.axis.types.Duration(false, years, 0, days, hours,
				minutes, seconds);
	}

	public static Duration fromApacheDuration(
			org.apache.axis.types.Duration aDur) {
		if (aDur == null)
			return null;

		long millis = aDur.getYears() * MILLISECONDS_PER_YEAR;
		millis += aDur.getMonths() * MILLISECONDS_PER_MONTH;
		millis += aDur.getDays() * MILLISECONDS_PER_DAY;
		millis += aDur.getHours() * MILLISECONDS_PER_HOUR;
		millis += aDur.getMinutes() * MILLISECONDS_PER_MINUTE;
		millis += aDur.getSeconds() * MILLISECONDS_PER_SECOND;

		return new Duration(millis);
	}
}