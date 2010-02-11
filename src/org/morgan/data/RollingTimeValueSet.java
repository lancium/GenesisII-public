package org.morgan.data;

import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.morgan.util.Pair;

public class RollingTimeValueSet<Type> extends TimeValueSet<Type>
{
	private long _windowSize;
	
	protected void trim(Calendar now)
	{
		Calendar limit = (Calendar)now.clone();
		limit.setTimeInMillis(limit.getTimeInMillis() - _windowSize);
		
		synchronized(_values)
		{
			while (!_values.isEmpty() && 
				_values.peekLast().first().before(limit))
					_values.removeLast();
		}
	}
	
	public RollingTimeValueSet(long windowSize, TimeUnit windowSizeUnits)
	{
		_windowSize = windowSizeUnits.toMillis(windowSize);
	}
	
	@Override
	public Pair<Calendar, Type> addValue(Type value)
	{
		synchronized(_values)
		{
			Pair<Calendar, Type> pair = super.addValue(value);
			trim(pair.first());
			
			return pair;
		}
	}

	@Override
	public Iterator<Pair<Calendar, Type>> iterator()
	{
		synchronized(_values)
		{
			trim(Calendar.getInstance());
			return super.iterator();
		}
	}
}