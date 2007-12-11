package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.WindowsIFSFile;

public class DataTracker {
	private static DataTracker myInstance = null;
	private Hashtable<Integer, WindowsIFSFile> openFiles;	
	private ReentrantLock lock;
	private int nextFileHandle;
	
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
		return openFiles.get(fileHandle);
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
