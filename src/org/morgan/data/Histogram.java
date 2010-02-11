package org.morgan.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.morgan.util.Pair;

public class Histogram<DataRangeType> implements Iterable<Pair<String, Integer>>
{
	static final private int DEFAULT_NUM_COLUMNS = 80;
	
	static private String toSimpleString(
		Collection<Pair<String, Integer>> items)
	{
		StringBuilder builder = new StringBuilder();
		for (Pair<String, Integer> item : items)
		{
			if (builder.length() > 0)
				builder.append('\n');
			
			builder.append(String.format("%s: %d",
				item.first(), item.second()));
		}
		
		return builder.toString();
	}
	
	static private <Type> List<Pair<Type, Integer>> createList(
		Pair<Type, Integer>...pairs)
	{
		List<Pair<Type, Integer>> ret = new Vector<Pair<Type,Integer>>(
			pairs.length);
		for (Pair<Type, Integer> item : pairs)
			ret.add(item);
		
		return ret;
	}
	
	static private int getDigitsAndSign(int value)
	{
		if (value == 0)
			return 1;
		if (value < 0)
			return (int)(Math.log10(-1 * value) + 2);
		
		return (int)(Math.log10(value) + 1);
	}
	
	private List<Pair<DataRangeType, Integer>> _bins;
	private int _minValue;
	private int _maxValue;
	
	private DataRangeLabelDelegate<DataRangeType> _labelDelegate;
	
	public Histogram(List<Pair<DataRangeType, Integer>> sortedItems,
		DataRangeLabelDelegate<DataRangeType> labelDelegate)
	{
		_bins = new Vector<Pair<DataRangeType,Integer>>(sortedItems);
		_labelDelegate = (labelDelegate == null) ?
			new DefaultDataRangeLabelDelegate() :
			labelDelegate;
			
		_minValue = Integer.MAX_VALUE;
		_maxValue = Integer.MIN_VALUE;
		
		for (Pair<DataRangeType, Integer> item : _bins)
		{
			_minValue = Math.min(_minValue, item.second().intValue());
			_maxValue = Math.max(_maxValue, item.second().intValue());
		}
	}
	
	public Histogram(List<Pair<DataRangeType, Integer>> sortedItems)
	{
		this(sortedItems, null);
	}
	
	public Histogram(DataRangeLabelDelegate<DataRangeType> labelDelegate,
		Pair<DataRangeType, Integer>...sortedItems)
	{
		this(createList(sortedItems), labelDelegate);
	}
	
	public Histogram(Pair<DataRangeType, Integer>...sortedItems)
	{
		this(null, sortedItems);
	}
	
	final public int minimumValue()
	{
		return _minValue;
	}
	
	final public int maximumValue()
	{
		return _maxValue;
	}
	
	final public int size()
	{
		return _bins.size();
	}
	
	final public boolean isEmpty()
	{
		return _bins.isEmpty();
	}

	@Override
	final public Iterator<Pair<String, Integer>> iterator()
	{
		Vector<Pair<String, Integer>> ret = new Vector<Pair<String,Integer>>(
			_bins.size());
		for (Pair<DataRangeType, Integer> item : _bins)
			ret.add(new Pair<String, Integer>(
				_labelDelegate.toString(item.first()),
				item.second()));
		
		return ret.iterator();
	}
	
	/**
	 * Returns a multi-line string with no line having more than the
	 * specified number of columns.  Each line will correspond to an
	 * item in the histo gram with the following format:
	 * 	<label>:  {marks} [<value>]
	 * Where {marks} is some number of minuses, or pluses (or a 0)
	 * indicating the scaled size of the value for that label.  An
	 * example output for values of -10, 0, and 10 might be
	 *  Minus 10: -----       [-10]
	 *  Zero    :      0      [  0]
	 *  Plus 10 :       +++++ [ 10]
	 *  
	 * @param columns
	 * @return
	 */
	final public String toString(int columns)
	{
		int maxValueDigits = Math.max(
			getDigitsAndSign(_minValue),
			getDigitsAndSign(_maxValue));
		int maxLabelLength = 0;
		int spaceLeftForMarks;
		
		Vector<Pair<String, Integer>> items = new Vector<Pair<String,Integer>>(
			_bins.size());
		
		for (Pair<DataRangeType, Integer> item : _bins)
		{
			String label = _labelDelegate.toString(item.first());
			items.add(new Pair<String, Integer>(label, item.second()));
			maxLabelLength = Math.max(maxLabelLength, label.length());
		}
		
		spaceLeftForMarks = columns - (maxLabelLength + 2) -
			(maxValueDigits + 3);
		
		if (spaceLeftForMarks < 9)
			return toSimpleString(items);
		
		String format = String.format("%%-%ds: %%s [%%%dd]",
			maxLabelLength, maxValueDigits);
		StringBuilder builder = new StringBuilder();
		MarkingContext context = new MarkingContext(
			_minValue, _maxValue, spaceLeftForMarks);
		
		for (Pair<String, Integer> item : items)
		{
			if (builder.length() > 0)
				builder.append('\n');
			
			String label = item.first();
			int value = item.second().intValue();
			
			builder.append(String.format(format,
				label, context.makeMarks(value),
				value));
		}
		
		return builder.toString();
	}
	
	@Override
	final public String toString()
	{
		return toString(DEFAULT_NUM_COLUMNS);
	}
	
	private class DefaultDataRangeLabelDelegate 
		implements DataRangeLabelDelegate<DataRangeType>
	{
		@Override
		final public String toString(DataRangeType dataRange)
		{
			return dataRange.toString();
		}	
	}
	
	static private class MarkingContext
	{
		private int _spaceForMarks;
		private int _zeroPosition;
		private int _spread;
		private double _valuesPerMark;
		
		private MarkingContext(int minValue, int maxValue,
			int spaceForMarks)
		{
			_spaceForMarks = spaceForMarks;
			if (minValue < 0)
			{
				if (maxValue > 0)
				{
					_spread = maxValue - minValue + 1;
					_zeroPosition = _spaceForMarks * (-1 * minValue) / _spread;
				} else
				{
					_spread = -1 * minValue + 1;
					_zeroPosition = _spaceForMarks - 1;
				}
			} else
			{
				_spread = maxValue + 1;
			}

			_valuesPerMark = (double)_spread / _spaceForMarks;
		}
		
		public String makeMarks(int value)
		{
			char []characters = new char[_spaceForMarks];
			Arrays.fill(characters, ' ');
			double counter = 0.0;
			int lcv = _zeroPosition;
			
			if (value == 0)
			{
				characters[_zeroPosition] = 'Z';
			} else if (value < 0)
			{
				for (; (counter >= value) && (lcv >= 0); lcv--, counter -= _valuesPerMark)
					characters[lcv] = '-';
			} else
			{
				for(; (counter <= value) && (lcv < characters.length); lcv++, counter += _valuesPerMark)
					characters[lcv] = '+';
			}
			
			return new String(characters);
		}
	}
}