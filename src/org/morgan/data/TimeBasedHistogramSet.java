package org.morgan.data;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.morgan.util.Pair;

public class TimeBasedHistogramSet extends RollingTimeValueSet<Integer>
{
	public TimeBasedHistogramSet(long windowSize, TimeUnit windowSizeUnits)
	{
		super(windowSize, windowSizeUnits);
	}
	
	public <DataRangeType> Histogram<DataRangeType> histogram(
		long timeInterval, TimeUnit timeIntervalUnits,
		HistogramDataCombiner<DataRangeType> dataCombiner,
		DataRangeLabelDelegate<DataRangeType> labelDelegate)
	{
		long interval = timeIntervalUnits.toMillis(timeInterval);
		LinkedList<Pair<Calendar, Integer>> values;
		Calendar now = Calendar.getInstance();
		TimeRange range = new TimeRange(
			now.getTimeInMillis() - interval, now.getTimeInMillis());
		
		synchronized(_values)
		{
			trim(now);
			values = new LinkedList<Pair<Calendar, Integer>>(_values);
		}
		
		List<Pair<DataRangeType, Integer>> histogramData = 
			new LinkedList<Pair<DataRangeType, Integer>>();
		
		while (!values.isEmpty())
		{
			Collection<Pair<Calendar, Integer>> combo =
				new LinkedList<Pair<Calendar,Integer>>();
			
			while (!values.isEmpty() && range.during(values.peekFirst().first()))
				combo.add(values.removeFirst());
			
			histogramData.add(dataCombiner.combine(
				range.beginning(), range.end(), combo));
			range = range.previous();
		}
		
		if (labelDelegate != null && 
			labelDelegate instanceof TimeAwareDataRangeLabelDelegate<?>)
			(TimeAwareDataRangeLabelDelegate.class.cast(labelDelegate)).markTime(now);
		
		return new Histogram<DataRangeType>(
			histogramData, labelDelegate);
	}
}
