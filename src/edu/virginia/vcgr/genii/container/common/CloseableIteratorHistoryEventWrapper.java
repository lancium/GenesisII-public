package edu.virginia.vcgr.genii.container.common;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.common.HistoryEventBundleType;
import edu.virginia.vcgr.genii.container.cservices.history.CloseableIterator;

class CloseableIteratorHistoryEventWrapper
	implements CloseableIterator<MessageElement>
{
	private CloseableIterator<HistoryEvent> _rootIter;
	
	CloseableIteratorHistoryEventWrapper(
		CloseableIterator<HistoryEvent> rootIter)
	{
		_rootIter = rootIter;
	}
	
	@Override
	final public void close() throws IOException
	{
		_rootIter.close();
	}

	@Override
	final public boolean hasNext()
	{
		return _rootIter.hasNext();
	}

	@Override
	final public MessageElement next()
	{
		try
		{
			HistoryEvent event = _rootIter.next();
			byte []data = DBSerializer.serialize(event, -1L);
			return new MessageElement(
				new QName("http://tempuri.org", "data"),
				new HistoryEventBundleType(data));
		}
		catch (IOException ioe)
		{
			throw new RuntimeException(
				"Unable to serialize history event.", ioe);
		}
	}

	@Override
	final public void remove()
	{
		_rootIter.remove();
	}
}
