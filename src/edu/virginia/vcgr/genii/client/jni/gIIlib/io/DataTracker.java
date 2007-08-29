package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

public class DataTracker {
	private static DataTracker myInstance = null;
	private Hashtable<Integer, InputStream> readFiles;
	private Hashtable<Integer, OutputStream> writeFiles;
	private ReentrantLock readLock;
	private ReentrantLock writeLock;
	
	private DataTracker(){
		readFiles = new Hashtable<Integer, InputStream>();
		writeFiles = new Hashtable<Integer, OutputStream>();
		readLock = new ReentrantLock();
		writeLock = new ReentrantLock();
	}
	
	public static DataTracker getInstance(){
		if(myInstance == null){
			myInstance = new DataTracker();			
		}
		return myInstance;				
	}
	
	public InputStream getReadStream(Integer fileHandle){
		InputStream stream;
		readLock.lock();
		stream = readFiles.get(fileHandle);
		readLock.unlock();
		return stream;
	}
	public OutputStream getWriteStream(Integer fileHandle){
		OutputStream stream;
		writeLock.lock();
		stream = writeFiles.get(fileHandle);
		writeLock.unlock();
		return stream;
	}
	
	public void putStream(Integer fileHandle, Object stream){		
		if(stream instanceof InputStream){
			readLock.lock();
			readFiles.put(fileHandle, (InputStream)stream);
			readLock.unlock();
		}
		else if(stream instanceof OutputStream){
			writeLock.lock();
			writeFiles.put(fileHandle, (OutputStream)stream);
			writeLock.unlock();
		}
		else{
			System.out.println("Invalid usage of putStream");
		}
	}
	
	public void removeStream(Integer fileHandle){	
		readLock.lock(); 
		writeLock.lock();
		readFiles.remove(fileHandle);		
		writeFiles.remove(fileHandle);
		writeLock.unlock(); 
		readLock.unlock(); 
	}
}
