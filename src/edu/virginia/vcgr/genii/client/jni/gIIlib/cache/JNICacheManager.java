package edu.virginia.vcgr.genii.client.jni.gIIlib.cache;

import java.util.Hashtable;

import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;

/* Cache Manager for the JNI Library */
public class JNICacheManager {
	
	private static JNICacheManager myManager = null;

	private Hashtable <String, JNICacheEntry> myCache = null;
	
	public JNICacheManager(){
		myCache = new Hashtable <String, JNICacheEntry>();
	}
	
	public static JNICacheManager getInstance(){
		if(myManager == null){
			myManager = new JNICacheManager();			
		}
		return myManager;				
	}
	
	public JNICacheEntry getCacheEntry(String path){
		JNICacheEntry entry = myCache.get(path);
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
		JNICacheEntry old = myCache.put(path, entry);
		if(JNILibraryBase.DEBUG && old != null){
			System.out.println("Replaced: " + old + "with " + entry);
		}		
	}	
}
