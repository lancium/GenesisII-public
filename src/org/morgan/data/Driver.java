package org.morgan.data;

import java.util.concurrent.TimeUnit;

public class Driver
{
	static public void main(String []args) throws Throwable
	{
		int count = 0;
		TimeBasedHistogramSet data = new TimeBasedHistogramSet(
			30, TimeUnit.SECONDS);
		long stopTime = System.currentTimeMillis() + 40 * 1000L;
		
		System.out.println("Iterating for 40 seconds...Please wait.");
		while (System.currentTimeMillis() < stopTime)
		{
			data.addValue(count++ - 20);
			
			Thread.sleep(1000L);
		}
		
		System.out.format("Histogram:\n%s\n",
			data.histogram(5, TimeUnit.SECONDS, HistogramDataCombiners.AverageCombiner,
				new RelativeTimeRangeLabelDelegate(TimeUnit.SECONDS)));
	}
}