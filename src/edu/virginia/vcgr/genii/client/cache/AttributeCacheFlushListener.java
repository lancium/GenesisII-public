package edu.virginia.vcgr.genii.client.cache;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.naming.WSName;

public interface AttributeCacheFlushListener
{
	/**
	 * Flush the contents of the attribute cache.
	 * 
	 * @param endpoint The endpoint whose attributes you want to
	 * flush.  If this is null, then the attributes from ALL endpoints
	 * will be flushed.
	 * @param attributes The qnames of the attributes to flush from
	 * the cache.  If this parameter is null or empty, then all attributes
	 * are flush (for the given endpoint), otherwise, only those specified
	 * are flushed.
	 */
	public void flush(WSName endpoint,
		QName...attributes);
}