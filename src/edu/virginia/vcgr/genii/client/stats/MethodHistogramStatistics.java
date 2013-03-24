package edu.virginia.vcgr.genii.client.stats;

import java.util.concurrent.TimeUnit;

import org.morgan.data.Histogram;
import org.morgan.data.HistogramDataCombiners;
import org.morgan.data.RelativeTimeRangeLabelDelegate;
import org.morgan.data.TimeBasedHistogramSet;
import org.morgan.data.TimeRange;

public class MethodHistogramStatistics
{
	private int _activeMethods = 0;
	private TimeBasedHistogramSet _histoData = new TimeBasedHistogramSet(1, TimeUnit.MINUTES);

	synchronized public void addActiveMethod()
	{
		_activeMethods++;
		_histoData.addValue(_activeMethods);
	}

	synchronized public void removeActiveMethod()
	{
		_activeMethods--;
		_histoData.addValue(_activeMethods);
	}

	public Histogram<TimeRange> histogram()
	{
		return _histoData.histogram(5, TimeUnit.SECONDS, HistogramDataCombiners.MaximumCombiner,
			new RelativeTimeRangeLabelDelegate(TimeUnit.SECONDS));
	}
}