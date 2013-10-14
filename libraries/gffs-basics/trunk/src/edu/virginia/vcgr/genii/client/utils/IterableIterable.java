package edu.virginia.vcgr.genii.client.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

final public class IterableIterable<Type> implements Iterable<Type>
{
	Collection<Iterable<Type>> _iterables;

	public IterableIterable(Iterable<Type> iterable)
	{
		this();
		_iterables.add(iterable);
	}

	public IterableIterable()
	{
		_iterables = new LinkedList<Iterable<Type>>();
	}

	final public void add(Iterable<Type> iterable)
	{
		_iterables.add(iterable);
	}

	@Override
	final public Iterator<Type> iterator()
	{
		return new IterableIterableIterator<Type>(_iterables.iterator());
	}

	static private class IterableIterableIterator<Type> implements Iterator<Type>
	{
		private Iterator<Iterable<Type>> _iterables;
		private Iterator<Type> _current;

		private void findNext()
		{
			while (true) {
				if (_current != null && _current.hasNext())
					return;

				if (!_iterables.hasNext()) {
					_current = null;
					return;
				}

				_current = _iterables.next().iterator();
			}
		}

		private IterableIterableIterator(Iterator<Iterable<Type>> iterables)
		{
			_iterables = iterables;
			findNext();
		}

		@Override
		final public boolean hasNext()
		{
			return (_current != null && _current.hasNext());
		}

		@Override
		final public Type next()
		{
			Type ret = _current.next();
			findNext();
			return ret;
		}

		@Override
		final public void remove()
		{
			throw new UnsupportedOperationException("Remove not supported!");
		}
	}
}