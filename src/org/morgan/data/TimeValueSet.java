package org.morgan.data;

import java.util.Calendar;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.morgan.util.Pair;

public class TimeValueSet<Type> implements Iterable<Pair<Calendar, Type>>
{
	protected Deque<Pair<Calendar, Type>> _values =
		new LinkedList<Pair<Calendar, Type>>();
	
	public Pair<Calendar, Type> addValue(Type value)
	{
		Calendar now;
		
		synchronized(_values)
		{
			now = Calendar.getInstance();
			_values.addFirst(new Pair<Calendar, Type>(
				now, value));
		}
		
		return new Pair<Calendar, Type>(
			now, value);
	}

	@Override
	public Iterator<Pair<Calendar, Type>> iterator()
	{
		Collection<Pair<Calendar, Type>> values;
		
		synchronized(_values)
		{
			values = new Vector<Pair<Calendar,Type>>(_values.size());
			for (Pair<Calendar, Type> pair : _values)
				values.add(new Pair<Calendar, Type>(
					pair.first(), pair.second()));
		}
		
		return values.iterator();
	}
}