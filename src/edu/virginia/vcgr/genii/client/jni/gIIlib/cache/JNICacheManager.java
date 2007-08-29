package edu.virginia.vcgr.genii.client.jni.gIIlib.cache;

import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;

/* Cache Manager for the JNI Library */
public class JNICacheManager {
	
	private static JNICacheManager myManager = null;
	private ReentrantLock cacheLock;
	private Hashtable <String, JNICacheEntry> myCache = null;
	
	public JNICacheManager(){
		myCache = new Hashtable <String, JNICacheEntry>();
		cacheLock = new ReentrantLock();
	}
	
	synchronized public static JNICacheManager getInstance(){
		if(myManager == null){
			myManager = new JNICacheManager();			
		}
		return myManager;				
	}
	
	public JNICacheEntry getCacheEntry(String path){
		cacheLock.lock();
		JNICacheEntry entry = myCache.get(path);
		cacheLock.unlock();
		if(JNILibraryBase.DEBUG)
		{
			if(entry == null){			
				System.out.println("Cache Miss: " + path);
			}
			else{
				System.out.println("Cache Hit: " + entry.toString());				
			}		
		}
		return entry;		
	}
	public void putCacheEntry(String path, JNICacheEntry entry){
		cacheLock.lock();
		JNICacheEntry old = myCache.put(path, entry);
		cacheLock.unlock();
		if(JNILibraryBase.DEBUG && old != null){
			System.out.println("Replaced: " + old + "with " + entry);
		}		
	}	
}
