package edu.virginia.vcgr.test.regression.client.units;

import org.junit.Assert;
import org.junit.Test;

import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;

public class TestDuration
{
	@Test
	public void testNonUnittedDurationParse()
	{
		Duration d = new Duration("7");
		Assert.assertEquals(d.as(DurationUnits.Milliseconds), 7.0, 0.001);
	}
}