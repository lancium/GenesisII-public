package edu.virginia.vcgr.genii.client.gfs.cache;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;
import edu.virginia.vcgr.genii.client.gfs.cache.objects.GeniiCachedResource;

/** 
 * This class manages the cache for g-icing
 */
public class GeniiCacheManager {	
	private Hashtable <String, GeniiCachedResource> _cache 
		= new Hashtable<String, GeniiCachedResource>();
	private int cacheLifeTime;
	private static GeniiCacheManager _manager;	
	private static GenesisIIFilesystem _fs = null;
	
	static private Log _logger = LogFactory.getLog(GeniiCacheManager.class);

	/**
	 * Instantiates a cache with the cache lifetime given
	 * @param cacheLifeInSeconds
	 */
	public GeniiCacheManager(int cacheLifeInSeconds, GenesisIIFilesystem fs){				
		cacheLifeTime = cacheLifeInSeconds;
		_fs = fs;
		_manager = this;
	}
	
	public static GeniiCacheManager getInstance(){
		return _manager;		
	}
	
	public synchronized GeniiCachedResource getCacheItem(String path, boolean doRefresh)
			throws FSException{			
		long currentTime = System.currentTimeMillis();							
		GeniiCachedResource resource = _cache.get(path);
		
		//If cache entry is stale (refresh entry)
		if((resource != null && 
				(currentTime - resource.getTimeOfEntry()) > (cacheLifeTime * 1000))){
			
			_logger.debug(String.format("Refreshing cache item %s.  Old time is " 
					+ "%d, new time is %d", path, resource.getTimeOfEntry(), currentTime));
			
			//Only refresh if a file or told to refresh (for dir's)
			if(doRefresh || !resource.isDirectory()) {
				resource.refresh();
				resource.setTimeOfEntry(currentTime);
			}
		}								
		return resource;
	}	
	
	public synchronized void putCacheItem(String path, GeniiCachedResource resource){				
		if(resource != null){		
			_cache.put(path, resource);
		}			
	}
	
	public synchronized GeniiCachedResource removeCacheItem(String path) {
		return _cache.remove(path);
	}

	public GenesisIIFilesystem get_fs() {
		return _fs;
	}	
}
