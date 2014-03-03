package edu.virginia.vcgr.genii.container.alarms;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AlarmTable implements Iterable<AlarmDescriptor>
{
	private Set<AlarmDescriptor> _alarmsSortedByOccurance = new TreeSet<AlarmDescriptor>(new OccuranceComparator());
	private Map<Long, AlarmDescriptor> _alarmsByID = new HashMap<Long, AlarmDescriptor>();

	public AlarmDescriptor peek()
	{
		Iterator<AlarmDescriptor> iter = _alarmsSortedByOccurance.iterator();
		if (iter.hasNext())
			return iter.next();

		return null;
	}

	public AlarmDescriptor get(long alarmID)
	{
		return _alarmsByID.get(alarmID);
	}

	public AlarmDescriptor remove(long alarmID)
	{
		AlarmDescriptor desc = _alarmsByID.remove(alarmID);
		if (desc != null)
			_alarmsSortedByOccurance.remove(desc);

		return desc;
	}

	public void put(long alarmID, Date nextOccurance)
	{
		AlarmDescriptor descriptor = new AlarmDescriptor(alarmID, nextOccurance);

		_alarmsSortedByOccurance.add(descriptor);
		_alarmsByID.put(alarmID, descriptor);
	}

	@Override
	public Iterator<AlarmDescriptor> iterator()
	{
		return new DescriptorIterator(_alarmsSortedByOccurance.iterator());
	}

	private class DescriptorIterator implements Iterator<AlarmDescriptor>
	{
		private Iterator<AlarmDescriptor> _iter;
		private AlarmDescriptor _current = null;

		public DescriptorIterator(Iterator<AlarmDescriptor> iter)
		{
			_iter = iter;
		}

		@Override
		public boolean hasNext()
		{
			return _iter.hasNext();
		}

		@Override
		public AlarmDescriptor next()
		{
			_current = _iter.next();
			return _current;
		}

		@Override
		public void remove()
		{
			if (_current != null)
				_alarmsByID.remove(_current.getAlarmID());
			_iter.remove();
			_current = null;
		}
	}

	static private class OccuranceComparator implements Comparator<AlarmDescriptor>
	{
		@Override
		public int compare(AlarmDescriptor o1, AlarmDescriptor o2)
		{
			long result = o1.getNextOccurance().compareTo(o2.getNextOccurance());
			if (result == 0)
				result = o1.getAlarmID() - o2.getAlarmID();

			if (result < 0)
				return -1;
			else if (result == 0)
				return 0;
			else
				return 1;
		}
	}
}