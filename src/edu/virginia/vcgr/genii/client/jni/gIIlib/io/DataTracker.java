package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

public class DataTracker {
	public static DataTracker myInstance = null;
	
	public Hashtable<Integer, InputStream> readFiles;
	public Hashtable<Integer, OutputStream> writeFiles;
	
	private DataTracker(){
		readFiles = new Hashtable<Integer, InputStream>();
		writeFiles = new Hashtable<Integer, OutputStream>();
	}
	
	public static DataTracker getInstance(){
		if(myInstance == null){
			myInstance = new DataTracker();			
		}
		return myInstance;				
	}
	
	public InputStream getReadStream(Integer fileHandle){
		return readFiles.get(fileHandle);
	}
	public OutputStream getWriteStream(Integer fileHandle){
		return writeFiles.get(fileHandle);
	}
	
	public void putStream(Integer fileHandle, Object stream){
		if(stream instanceof InputStream){
			readFiles.put(fileHandle, (InputStream)stream);
		}
		else if(stream instanceof OutputStream){
			writeFiles.put(fileHandle, (OutputStream)stream);
		}
		else{
			System.out.println("Invalid usage of putStream");
		}
	}
	
	public void removeStream(Integer fileHandle){		
		readFiles.remove(fileHandle);		
		writeFiles.remove(fileHandle);
	}
}
