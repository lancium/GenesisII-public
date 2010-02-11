package org.morgan.data;

import java.util.Calendar;
import java.util.Collection;

import org.morgan.util.Pair;

public interface HistogramDataCombiner<DataRangeType>
{
	public Pair<DataRangeType, Integer> combine(Calendar minimumTime,
		Calendar maximumTime, Collection<Pair<Calendar, Integer>> data);
}