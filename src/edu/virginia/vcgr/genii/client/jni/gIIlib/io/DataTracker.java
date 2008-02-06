package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.WindowsIFSFile;

public class DataTracker {
	private static DataTracker myInstance = null;
	private Hashtable<Integer, WindowsIFSFile> openFiles;	
	private ReentrantLock lock;
	private int nextFileHandle;
	
	//Number of IO calls received
	private int counter = 0;
	
	//Number of IO calls before GC is forced
	private final static int MAX_CALLS_BEFORE_GC = 1000; 
	
	private DataTracker(){
		openFiles = new Hashtable<Integer, WindowsIFSFile>();		
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
	
	public WindowsIFSFile getFile(Integer fileHandle){
		WindowsIFSFile theFile;
		lock.lock();			
		
		counter++;
		
		//FORCE garbage collection after a certain amount of calls
		if(counter > MAX_CALLS_BEFORE_GC){
			System.out.println("Garbage Collecting:  Amount of Free Memory for Java -  " + 
					(Runtime.getRuntime().freeMemory() /1024 * 1024) + "MB");
			System.gc();			
			counter = 0;
		}
		
		theFile = openFiles.get(fileHandle);		
		lock.unlock();		
		return theFile;
	}
	
	public void putFile(Integer fileHandle, WindowsIFSFile file){		
		lock.lock();
		openFiles.put(fileHandle, file);
		lock.unlock();
	}
	
	public void removeFile(Integer fileHandle){	
		lock.lock();
		openFiles.remove(fileHandle); 
		lock.unlock(); 
	}
}
