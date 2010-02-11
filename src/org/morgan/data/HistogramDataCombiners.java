package org.morgan.data;

import java.util.Calendar;
import java.util.Collection;

import org.morgan.util.Pair;

public class HistogramDataCombiners
{
	static private class HistogramDataSummer 
		implements HistogramDataCombiner<TimeRange>
	{
		@Override
		public Pair<TimeRange, Integer> combine(
			Calendar minimumTime, Calendar maximumTime,
			Collection<Pair<Calendar, Integer>> data)
		{
			int sum = 0;
			
			for (Pair<Calendar, Integer> item : data)
				sum += item.second();
			
			return new Pair<TimeRange, Integer>(
				new TimeRange(minimumTime, maximumTime),
					sum);
		}
	}
	
	static private class HistogramDataAverager 
		implements HistogramDataCombiner<TimeRange>
	{
		@Override
		public Pair<TimeRange, Integer> combine(
			Calendar minimumTime, Calendar maximumTime,
			Collection<Pair<Calendar, Integer>> data)
		{
			int sum = 0;
			
			for (Pair<Calendar, Integer> item : data)
				sum += item.second();
			
			if (data.size() == 0)
				return new Pair<TimeRange, Integer>(
					new TimeRange(minimumTime, maximumTime),
					sum);
			
			return new Pair<TimeRange, Integer>(
				new TimeRange(minimumTime, maximumTime),
					sum / data.size());
		}
	}
	
	static private class HistogramDataMaximizer 
		implements HistogramDataCombiner<TimeRange>
	{
		@Override
		public Pair<TimeRange, Integer> combine(
			Calendar minimumTime, Calendar maximumTime,
			Collection<Pair<Calendar, Integer>> data)
		{
			int value = Integer.MIN_VALUE;
			
			for (Pair<Calendar, Integer> item : data)
				value = Math.max(value, item.second().intValue());
			
			if (data.size() == 0)
				return new Pair<TimeRange, Integer>(
					new TimeRange(minimumTime, maximumTime),
					0);
			
			return new Pair<TimeRange, Integer>(
				new TimeRange(minimumTime, maximumTime),
					value);
		}
	}
	
	static private class HistogramDataMinimizer
		implements HistogramDataCombiner<TimeRange>
	{
		@Override
		public Pair<TimeRange, Integer> combine(
			Calendar minimumTime, Calendar maximumTime,
			Collection<Pair<Calendar, Integer>> data)
		{
			int value = Integer.MAX_VALUE;
			
			for (Pair<Calendar, Integer> item : data)
				value = Math.min(value, item.second().intValue());
			
			if (data.size() == 0)
				return new Pair<TimeRange, Integer>(
					new TimeRange(minimumTime, maximumTime),
					0);
			
			return new Pair<TimeRange, Integer>(
				new TimeRange(minimumTime, maximumTime),
					value);
		}
	}
	
	static public HistogramDataCombiner<TimeRange> SumCombiner =
		new HistogramDataSummer();
	static public HistogramDataCombiner<TimeRange> AverageCombiner =
		new HistogramDataAverager();
	static public HistogramDataCombiner<TimeRange> MinimumCombiner =
		new HistogramDataMinimizer();
	static public HistogramDataCombiner<TimeRange> MaximumCombiner =
		new HistogramDataMaximizer();
}