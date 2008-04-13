package edu.virginia.vcgr.genii.client.jni.gIIlib.cache;

import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsResourceHandle;

/** 
 * This class manages the cache for g-icing
 */
public class CacheManager {
	
	private static CacheManager _manager = null;
	private ReentrantLock cacheLock;
	private ReentrantLock handleLock;
	private Hashtable <String, CachedResource> _cache = null;
	private Hashtable <Integer, WindowsResourceHandle> _handleMap;

	//Number of seconds before Cache entry is removed
	private static final int CACHE_LIFE = 30; 

	public CacheManager(){
		_cache = new Hashtable<String, CachedResource>();
		_handleMap = new Hashtable<Integer, WindowsResourceHandle>();
		cacheLock = new ReentrantLock();
		handleLock= new ReentrantLock();
	}
	
	public synchronized static CacheManager getInstance(){
		if(_manager == null){
			_manager = new CacheManager();
		}
		return _manager;
	}
	
	public CachedResource getResource(String path){
		CachedResource resource;
		long currentTime = System.currentTimeMillis();
		cacheLock.lock();					
			resource = _cache.get(path);
			
			//If cache entry is stale (refresh entry)
			if((resource != null && 
					(currentTime - resource.getTimeOfEntry()) > (CACHE_LIFE * 1000))){
				if(resource instanceof CachedDir){
					((CachedDir)resource).refreshDirectoryEntries();
				}else{
					try{
						((CachedFile)resource).reconnectToEpr(false);
					}catch(Exception e)
					{e.printStackTrace();}
				}
				resource.setTimeOfEntry(System.currentTimeMillis());
			}						
		cacheLock.unlock();
		return resource;
	}
	
	public void putResource(String path, CachedResource resource){		
		cacheLock.lock();
			if(resource == null){
				_cache.remove(path);
			}
			else{
				_cache.put(path, resource);
			}
		cacheLock.unlock();		
	}
	
	public void addHandle(WindowsResourceHandle theHandle){
		handleLock.lock();
			_handleMap.put(theHandle.fileHandle, theHandle);
		handleLock.unlock();
	}
	
	public WindowsResourceHandle getHandle(Integer fileHandle){
		WindowsResourceHandle toReturn = null;
		handleLock.lock();
			toReturn = _handleMap.get(fileHandle);
		handleLock.unlock();
		return toReturn;
	}
	
	public void removeHandle(Integer fileHandle){
		handleLock.lock();
			_handleMap.remove(fileHandle);
		handleLock.unlock();
	}	
}
