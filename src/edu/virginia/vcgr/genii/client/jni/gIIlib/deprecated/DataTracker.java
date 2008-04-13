package edu.virginia.vcgr.genii.client.jni.gIIlib.deprecated;

import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;


public class DataTracker {
	private static DataTracker myInstance = null;
	private Hashtable<Integer, IFSResource> openResources;	
	private ReentrantLock lock;
	private int nextFileHandle;
	
	//Number of IO calls received
	private int counter = 0;
	
	//Number of IO calls before GC is forced
	private final static int MAX_CALLS_BEFORE_GC = 1000; 
	
	private DataTracker(){
		openResources = new Hashtable<Integer, IFSResource>();		
		lock = new ReentrantLock();
		nextFileHandle = 0;
	}
	
	public static DataTracker getInstance(){
		if(myInstance == null){
			myInstance = new DataTracker();			
		}
		return myInstance;				
	}
	
	public synchronized int atomicGetAndIncrementHandle(){		
		int lastFileHandle = nextFileHandle;
		nextFileHandle++;
		return lastFileHandle;
	}
	
	public IFSResource getResource(Integer handle){
		IFSResource theResource;
		lock.lock();			
		
		counter++;
		
		//FORCE garbage collection after a certain amount of calls
		if(counter > MAX_CALLS_BEFORE_GC){
			System.out.println("Garbage Collecting:  Amount of Free Memory for Java -  " + 
					(Runtime.getRuntime().freeMemory() /1024 * 1024) + "MB");
			System.gc();			
			counter = 0;
		}
		
		theResource = openResources.get(handle);		
		lock.unlock();		
		return theResource;
	}
	
	public void putResource(Integer handle, IFSResource resource){		
		lock.lock();
		openResources.put(handle, resource);
		lock.unlock();
	}
	
	public void removeResource(Integer handle){	
		lock.lock();
		openResources.remove(handle); 
		lock.unlock(); 
	}
}
