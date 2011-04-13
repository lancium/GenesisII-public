package edu.virginia.vcgr.genii.container.iterator;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.axis.message.MessageElement;

public abstract class AbstractIteratorBuilder<SourceType> 
	implements IteratorBuilder<SourceType>, Iterable<MessageElement>
{
	private int _preferredBatchSize = DEFAULT_PREFERRED_BATCH_SIZE;
	private List<Iterator<?>> _iterators = 
		new LinkedList<Iterator<?>>();
	
	abstract protected MessageElement serialize(SourceType item)
		throws IOException;
	
	@Override
	final public int preferredBatchSize()
	{
		return _preferredBatchSize;
	}

	@Override
	final public void preferredBatchSize(int preferredBatchSize)
	{
		_preferredBatchSize = preferredBatchSize;
	}

	@Override
	final public void addElements(Iterable<?> iterable)
	{
		_iterators.add(iterable.iterator());
	}

	@Override
	final public void addElements(Iterator<?> iterator)
	{
		_iterators.add(iterator);
	}

	@Override
	final public Iterator<MessageElement> iterator()
	{
		return new InternalIterator();
	}
	
	final private class InternalIterator implements Iterator<MessageElement>
	{
		private Iterator<Iterator<?>> _iterators;
		private Iterator<?> _current = null;
		
		private InternalIterator()
		{
			_iterators = AbstractIteratorBuilder.this._iterators.iterator();
			
			while(_iterators.hasNext())
			{
				_current = _iterators.next();
				if (_current.hasNext())
					break;
			}
		}

		@Override
		final public boolean hasNext()
		{
			return _current != null && _current.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		final public MessageElement next()
		{
			SourceType next = (SourceType)_current.next();
			if (!_current.hasNext())
			{
				while(_iterators.hasNext())
				{
					_current = _iterators.next();
					if (_current.hasNext())
						break;
				}
			}
			
			try
			{
				return serialize(next);
			}
			catch (IOException ioe)
			{
				throw new UndeclaredThrowableException(ioe);
			}
		}

		@Override
		final public void remove()
		{
			throw new UnsupportedOperationException(
				"Remove not supported on this iterator type!");
		}
	}
}