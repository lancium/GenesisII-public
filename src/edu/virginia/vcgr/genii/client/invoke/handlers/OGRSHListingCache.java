package edu.virginia.vcgr.genii.client.invoke.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	static private Log _logger = LogFactory.getLog(OGRSHListingCache.class);
	
	static private final int _MAX_CACHE_ELEMENTS = 1024;
	static private final long _DEFAULT_TIMEOUT_MS = 1000 * 15;
	
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
				(_entryName.equals(other._entryName));
		}
		
		public boolean equals(Object other)
		{
			if (!(other instanceof EntryKey))
				return false;
			
			return equals((EntryKey)other);
		}
		
		public String toString()
		{
			return "[" + _dirName.getEndpointIdentifier() + ":" + _entryName + "]";
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
			_logger.debug("Looking up all entries of directory \"" + dirName.getEndpointIdentifier() + "\".");
			
			ListResponse resp = (ListResponse)ctxt.proceed();
			for (EntryType entry : resp.getEntryList())
			{
				EntryKey key = new EntryKey(dirName, entry.getEntry_name());
				_logger.debug("Putting entry " + key + " into the cache.");
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
			
			_logger.debug("Looking for " + key + " in the cache.");
			synchronized(_entryCache)
			{
				ret = _entryCache.get(key);
			}
			
			if (ret != null)
			{
				_logger.debug("Found entry " + key + " in the cache.");
				return new ListResponse(new EntryType[] { ret } );
			}
			
			_logger.debug("Lookup up entry \"" + exp + "\" in directory \"" + dirName.getEndpointIdentifier() + "\".");
			ListResponse resp = (ListResponse)ctxt.proceed();
			EntryType []entries = resp.getEntryList();
			
			if ((entries != null) && (entries.length == 1))
			{
				_logger.debug("Adding entry " + key + " to the cache.");
				synchronized(_entryCache)
				{
					_entryCache.put(key, entries[0]);
				}
			}
			
			return resp;
		}
	}
}
