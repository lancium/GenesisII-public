package edu.virginia.vcgr.genii.client.jni.gIIlib.cache;

import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;

/* Cache Manager for the JNI Library */
public class JNICacheManager {
	
	private static final int MAX_SIZE = 300;
	private static final float LOAD_FACTOR = (float)0.5;
	private static JNICacheManager myManager = null;
	private ReentrantLock cacheLock;
	private Hashtable <String, JNICacheEntry> myCache = null;
	private JNICacheEntry eldest, lastInserted;
	
	public JNICacheManager(){
		//This crazy math keeps the hashtable size constant
		myCache = new Hashtable <String, JNICacheEntry>
			((int)(5+ ((float)MAX_SIZE) / LOAD_FACTOR),LOAD_FACTOR);
		cacheLock = new ReentrantLock();
	}
	
	synchronized public static JNICacheManager getInstance(){
		if(myManager == null){
			myManager = new JNICacheManager();			
		}
		return myManager;				
	}
	
	public JNICacheEntry getCacheEntry(String path){
		long currentTime = System.currentTimeMillis();
		cacheLock.lock();
		JNICacheEntry entry = myCache.get(path);			
		
		if(entry == null){			
			System.out.println("Cache Miss: " + path);
		}
		else{
			if((currentTime - entry.getTimeOfEntry()) < (30 * 1000)){
				System.out.println("Cache Hit: " + entry.toString());					
			}
			else{					
				System.out.println("Cache Miss: " + path + ". Time Expired");
				myCache.remove(path);
				//Fix links
				if(entry.previous != null){
					entry.previous.next = entry.next;
				}
				if(entry.next != null){
					entry.next.previous = entry.previous;
				}
				//Fix static pointers
				if(entry == lastInserted){
					lastInserted = lastInserted.previous;					
				}
				if(entry == eldest){
					eldest = eldest.next;
				}
			}				
		}
		cacheLock.unlock();
		return entry;		
	}
	public void putCacheEntry(String path, JNICacheEntry entry){
		cacheLock.lock();		
						
		JNICacheEntry old = myCache.put(path, entry);
				
		//Let's deal with Dbly linked lists for cache efficiency
		if(myCache.size() == 1){
			eldest = lastInserted = entry;			
		}
		else if(myCache.size() > MAX_SIZE){
			myCache.remove(eldest.getPath());
			if(JNILibraryBase.DEBUG){
				System.out.println("GIILibCache:  Removing " + eldest.getPath() + ". " +
					"Cache too large!");
			}
			eldest = eldest.next;
		}
		else{
			lastInserted.next = entry;
			entry.previous = lastInserted;
			lastInserted = entry;			
		}
		
		cacheLock.unlock();
		if(JNILibraryBase.DEBUG && old != null){
			System.out.println("Replaced: " + old + "with " + entry);
		}		
	}	
}
