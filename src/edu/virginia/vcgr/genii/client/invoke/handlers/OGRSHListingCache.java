package edu.virginia.vcgr.genii.client.invoke.handlers;

import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.RNSPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.invoke.InvocationContext;
import edu.virginia.vcgr.genii.client.invoke.PipelineProcessor;
import edu.virginia.vcgr.genii.client.naming.WSName;

public class OGRSHListingCache
{
	static private final int _MAX_CACHE_ELEMENTS = 1024;
	static private final long _DEFAULT_TIMEOUT_MS = 1000 * 5;
	
	static private class EntryKey
	{
		private WSName _dirName;
		private String _entryName;
		
		private int _hashCode;
		
		public EntryKey(WSName dirName, String entryName)
		{
			_dirName = dirName;
			_entryName = entryName;
			
			_hashCode = _dirName.hashCode() ^ _entryName.hashCode();
		}
		
		public int hashCode()
		{
			return _hashCode;
		}
		
		public boolean equals(EntryKey other)
		{
			return (_hashCode == other._hashCode) &&
				_dirName.equals(other._dirName) &&
				(_entryName == other._entryName);
		}
		
		public boolean equals(Object other)
		{
			if (!(other instanceof EntryKey))
				return false;
			
			return equals((EntryKey)other);
		}
	}
	
	private TimedOutLRUCache<EntryKey, EntryType> _entryCache =
		new TimedOutLRUCache<EntryKey, EntryType>(_MAX_CACHE_ELEMENTS, _DEFAULT_TIMEOUT_MS);
	
	@PipelineProcessor(portType = RNSPortType.class)
	public ListResponse list(InvocationContext ctxt,
		List listRequest) throws Throwable
	{
		EndpointReferenceType dirEPR = ctxt.getTarget();
		WSName dirName = new WSName(dirEPR);
		
		if (!dirName.isValidWSName())
		{
			// Can only cache WSNames
			return (ListResponse)ctxt.proceed();
		}
		
		String exp = listRequest.getEntry_name_regexp();
		if (exp.equals(".*"))
		{
			ListResponse resp = (ListResponse)ctxt.proceed();
			for (EntryType entry : resp.getEntryList())
			{
				EntryKey key = new EntryKey(dirName, entry.getEntry_name());
				synchronized(_entryCache)
				{
					_entryCache.put(key, entry);
				}
			}
			
			return resp;
		} else
		{
			EntryKey key = new EntryKey(dirName, exp);
			EntryType ret;
			
			synchronized(_entryCache)
			{
				ret = _entryCache.get(key);
			}
			
			if (ret != null)
				return new ListResponse(new EntryType[] { ret } );
			
			ListResponse resp = (ListResponse)ctxt.proceed();
			EntryType []entries = resp.getEntryList();
			
			if ((entries != null) && (entries.length == 1))
			{
				synchronized(_entryCache)
				{
					_entryCache.put(key, entries[0]);
				}
			}
			
			return resp;
		}
	}
}