package edu.virginia.vcgr.genii.client.iterator;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.apache.axis.message.MessageElement;
import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorEntry;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorWrapper;

public class WSIteratorConstructionParameters extends ConstructionParameters implements Closeable
{
	static final long serialVersionUID = 0L;
	
	static private Map<String, WSIteratorConstructionParameters> _originalConsParms =
		new HashMap<String, WSIteratorConstructionParameters>();
	
	transient private int _preferredBatchSize;

	transient private Iterator<MessageElement> _contentsIterator;
	
	transient private String _key;
	
	transient private boolean _isIndexedIterator;
		
	transient private InMemoryIteratorWrapper _imiw;
	
	/* For JAXB Only */
	@SuppressWarnings("unused")
	private WSIteratorConstructionParameters()
	{
	}
	
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
		
		_isIndexedIterator = original._isIndexedIterator;
		_contentsIterator = original._contentsIterator;
		_imiw = original._imiw;
	}
	
	@Override
	final protected void finalize() throws Throwable
	{
		close();
	}
	
	public WSIteratorConstructionParameters(Iterator<MessageElement> contentsIterator,
		int preferredBlockSize)
	{
		this(contentsIterator, preferredBlockSize, null);
	}
	
	public WSIteratorConstructionParameters(Iterator<MessageElement> contentsIterator,
			int preferredBlockSize, InMemoryIteratorWrapper imiw) 
	{
	//List<InMemoryIteratorEntry> indices
		synchronized(this)
		{
			_key = new GUID().toString();
			_originalConsParms.put(_key, this);
		}
		
		/*We don't need to fill the first block as we have to be disjoint with
		 * what is returned
		*/
		
		if (contentsIterator.hasNext())
			_contentsIterator = contentsIterator;
			
		else
			_contentsIterator = null;
		
		_preferredBatchSize = preferredBlockSize;
		
		if(imiw == null || imiw.getIndices() == null || imiw.getIndices().size() == 0)
		{
			_isIndexedIterator = false;
			_imiw = null;
		}
		
			
		else
		{
			if(_contentsIterator == null)
			{
				_isIndexedIterator = true;
				_imiw = imiw;
			}
			else
			{
				_isIndexedIterator = false;
				_imiw = null; ////only serialized or id-based !
			}
		}				
		
	}

	final public int preferredBatchSize()
	{
		return _preferredBatchSize;
	}
	
	
	
	final public Iterator<MessageElement> getContentsIterator()
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

	public boolean remainingContents(Object tempObj)
	{
		if(_contentsIterator!=null || _isIndexedIterator)
			return true;
		return false;
	}

	public boolean isIndexedIterator()
	{
		return _isIndexedIterator;
	}

	public List <InMemoryIteratorEntry> getIndices() 
	{
		return _imiw.getIndices();
	}
	
	public InMemoryIteratorWrapper getWrapper()
	{
		return _imiw;
	}
}