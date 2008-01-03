package edu.virginia.vcgr.genii.testing.utils.units;

import java.text.ParseException;

import org.junit.Test;
import org.junit.Assert;

import edu.virginia.vcgr.genii.client.utils.units.Duration;

public class DurationTest
{
	@Test
	public void testParser() throws ParseException
	{
		long expectedValue = 
			(1 * 365 * 24 * 60 * 60 * 1000L) +
			(2 * 7 * 24 * 60 * 60 * 1000L) +
			(3 * 24 * 60 * 60 * 1000L) +
			(4 * 60 * 60 * 1000L) +
			(5 * 60 * 1000L) +
			(6 * 1000L) +
			(7);
		
		Duration d = Duration.parse("1 year, 2 weeks, 3 days, 4 hours, " +
			"5 minutes, 6 seconds, 7 milliseconds");		
		Assert.assertEquals(expectedValue, d.getMilliseconds());
		
		d = Duration.parse("1year, 2weeks, 3days, 4hours, " +
			"5minutes, 6seconds,7 milliseconds");		
		Assert.assertEquals(expectedValue, d.getMilliseconds());
		
		d = Duration.parse("1 y, 2 w, 3 d, 4 h, " +
			"5 m, 6 s, 7 ms");		
		Assert.assertEquals(expectedValue, d.getMilliseconds());
		
		d = Duration.parse("1year2weeks3days4hours," +
			"5minutes6seconds7milliseconds");		
		Assert.assertEquals(expectedValue, d.getMilliseconds());
		
		d = Duration.parse("1y2w3d4h5m6s7ms");		
		Assert.assertEquals(expectedValue, d.getMilliseconds());
	}
}