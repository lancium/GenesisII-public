package edu.virginia.vcgr.genii.client.iterator;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.apache.axis.message.MessageElement;
import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.client.common.ConstructionParameters;

public class WSIteratorConstructionParameters extends ConstructionParameters implements Closeable
{
	static final long serialVersionUID = 0L;
	
	static private Map<String, WSIteratorConstructionParameters> _originalConsParms =
		new HashMap<String, WSIteratorConstructionParameters>();
	
	transient private int _preferredBatchSize;
	transient private MessageElement []_firstBlock;
	transient private Iterator<MessageElement> _contentsIterator;
	
	transient private String _key;
	
	/* For JAXB Only */
	@SuppressWarnings("unused")
	private WSIteratorConstructionParameters()
	{
	}
	
	@SuppressWarnings("unused")
	@XmlElement(namespace = IteratorConstants.ITERATOR_NS,
		name = "iterator-key", nillable = false, required = true)
	private String getKey()
	{
		return _key;
	}
	
	@SuppressWarnings("unused")
	private void setKey(String key)
	{
		_key = key;
		WSIteratorConstructionParameters original = 
			_originalConsParms.get(_key);
		if (original == null)
			throw new IllegalStateException(
				"Can't find original construction parameters!");
		_firstBlock = original._firstBlock;
		_contentsIterator = original._contentsIterator;
	}
	
	@Override
	final protected void finalize() throws Throwable
	{
		close();
	}
	
	public WSIteratorConstructionParameters(Iterator<MessageElement> contentsIterator,
		int preferredBlockSize)
	{
		synchronized(this)
		{
			_key = new GUID().toString();
			_originalConsParms.put(_key, this);
		}
		
		ArrayList<MessageElement> firstBlock = new ArrayList<MessageElement>(
			preferredBlockSize);
		
		for (int lcv = 0; lcv < preferredBlockSize && contentsIterator.hasNext(); lcv++)
		{
			MessageElement item = contentsIterator.next();
			
			firstBlock.add(item);
		}
		
		_firstBlock = firstBlock.toArray(new MessageElement[firstBlock.size()]);
		if (contentsIterator.hasNext())
			_contentsIterator = contentsIterator;
		else
			_contentsIterator = null;
		
		_preferredBatchSize = preferredBlockSize;
	}
	
	final public int preferredBatchSize()
	{
		return _preferredBatchSize;
	}
	
	final public MessageElement[] firstBlock()
	{
		return _firstBlock;
	}
	
	final public Iterator<MessageElement> remainingContents()
	{
		return _contentsIterator;
	}

	@Override
	final synchronized public void close() throws IOException
	{
		if (_key != null)
		{
			_originalConsParms.remove(_key);
			_key = null;
		}
	}
}