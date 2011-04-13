package edu.virginia.vcgr.genii.client.rns;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.ggf.rns.LookupResponseType;
import org.ggf.rns.RNSEntryResponseType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.iterator.WSIterable;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.iterator.IteratorInitializationType;

final public class RNSIterable implements Iterable<RNSEntryResponseType>
{
	private WSIterable<RNSEntryResponseType> _iterable;
	
	public RNSIterable(LookupResponseType lookupResponse,
		ICallingContext callContext, int blockSize)
			throws ResourceException, GenesisIISecurityException
	{
		RNSEntryResponseType []tmp = lookupResponse.getEntryResponse();
		
		_iterable = WSIterable.axisIterable(
			RNSEntryResponseType.class,
			tmp,
			new IteratorInitializationType(
				lookupResponse.getIterator(),
				null), callContext, blockSize);
	}
	
	final public RNSEntryResponseType[] toArray()
	{
		Collection<RNSEntryResponseType> tmp = 
			new LinkedList<RNSEntryResponseType>();
		
		for (RNSEntryResponseType resp : this)
			tmp.add(resp);
		
		return tmp.toArray(new RNSEntryResponseType[tmp.size()]);
	}
	
	final public Map<String, RNSEntryResponseType> toMap()
	{
		Map<String, RNSEntryResponseType> ret = 
			new HashMap<String, RNSEntryResponseType>();
		
		for (RNSEntryResponseType resp : this)
			ret.put(resp.getEntryName(), resp);
		
		return ret;
	}
	
	@Override
	final public Iterator<RNSEntryResponseType> iterator()
	{
		return new RNSIterator(_iterable.iterator());
	}
	
	static private class RNSIterator implements Iterator<RNSEntryResponseType>
	{
		private Iterator<RNSEntryResponseType> _iter;
		
		private RNSIterator(Iterator<RNSEntryResponseType> iter)
		{
			_iter = iter;
		}

		@Override
		final public boolean hasNext()
		{
			return _iter.hasNext();
		}

		@Override
		final public RNSEntryResponseType next()
		{
			RNSEntryResponseType resp = _iter.next();
			if (resp.getFault() != null)
				throw new RuntimeException(
					"Fault encountered with RNS entry!", resp.getFault());
			
			return resp;
		}

		@Override
		final public void remove()
		{
			_iter.remove();
		}
	}
}