package edu.virginia.vcgr.appmgr.patch.builder;

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorEnumeration<E> implements Enumeration<E>
{
	private Iterator<E> _iterator;

	public IteratorEnumeration(Iterator<E> iterator)
	{
		_iterator = iterator;
	}

	@Override
	public boolean hasMoreElements()
	{
		return _iterator.hasNext();
	}

	@Override
	public E nextElement()
	{
		return _iterator.next();
	}
}