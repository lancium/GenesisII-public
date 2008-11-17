package edu.virginia.vcgr.genii.client.jni.gIIlib.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.jni.gIIlib.ResourceInformation;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsDirHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsFileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsResourceHandle;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

/** 
 * This class is responsible for caching all information obtained from RNS for a directory
 * This class has optimized synchronization and assume that all dirHandles access this from 
 * different threads.  It assumes the common case for access to its directory entries are reads i.e.
 * reading the entries versus modifying the list.
 */
public class CachedDir extends CachedResource {		

	/*
	 * Lock used to protect access to directory entries list
	 * R/W capability in order to protect access  without undo burden to processes
	 */
	private ReentrantReadWriteLock dirEntriesLock = new ReentrantReadWriteLock(true);
	
	//Handles to entries for this directory (all items are cached!!!)
	private Hashtable<String, WindowsResourceHandle> directoryEntries;
	
	/* 
	 * A list of handles that are referencing me.  Since Writes are common-case, we synchronize
	 * on the object
	 */
	
	private ArrayList <WindowsDirHandle> myHandles = new ArrayList<WindowsDirHandle>();	
	
	public CachedDir(RNSPath dirPath, Integer desiredAccess) 
			throws IOException, RNSException{
		super();
		
		rnsPath = dirPath;		
		if(desiredAccess != WindowsResourceHandle.INFORMATION_ONLY){
			refreshDirectoryEntries();
		}else{
			setDirty(true);
		}
		isDirectory = true;
	}
	
	/**
	 * Attach directory handle to this cached directory
	 */
	public void attach(WindowsDirHandle dh){
		synchronized(myHandles){
			myHandles.add(dh);
		}
	}
	
	public void detatch(WindowsDirHandle dh){
		synchronized(myHandles){
			myHandles.remove(dh);
		}			
	}
	
	/** 
	 * Invalidates File Handles that are referring to this entry
	 * @param warnOnValidate
	 */
	public void invalidate(boolean warnOnValidate){
		synchronized(myHandles){
			if(!invalidated){
				invalidated = true;
				if(warnOnValidate && myHandles.size() > 0){
					System.out.println("Invalidating " + myHandles.size() + " handles for " +
							rnsPath.getName());
				}			
				for(WindowsDirHandle dh : myHandles)
				{			
					dh.invalidate();					
				}
				myHandles.clear();
				invalidated = true;
			}			
		}
	}		
			
	public ArrayList<ResourceInformation> getEntries(String target){				
		ArrayList<ResourceInformation> toReturn = new ArrayList<ResourceInformation>();
				
		
		if(isDirty()){						
			refreshDirectoryEntries();							
		}
				
		dirEntriesLock.readLock().lock();
		{	
			for(WindowsResourceHandle file : directoryEntries.values()){
				if(file.getCachedInformation().name.matches(target)){
					toReturn.add(file.getCachedInformation());
				}
			}
		}
		dirEntriesLock.readLock().unlock();	
		
		return toReturn;
	}
	
	public ResourceInformation getCachedInformation(int fileHandle){
		/* No extra synchronized is required since these times are hardset */
		return new ResourceInformation(true, rnsPath.getName(), 
				fileHandle, lastAccessedTime, lastModifiedTime, createTime, 0);
	}	
		
	public void refreshDirectoryEntries(){
		
		dirEntriesLock.writeLock().lock();						
		
		Date temp = new Date();														
		
		//Set times
		lastAccessedTime = temp;
		lastModifiedTime = temp;
		createTime = temp;
		
		directoryEntries = new Hashtable<String, WindowsResourceHandle>();
		
		//ALWAYS get all entries
		try{
			Collection<RNSPath> entries = rnsPath.listContents();
			
			for(RNSPath entry : entries){
				EndpointReferenceType et = entry.getEndpoint();
				TypeInformation ti = new TypeInformation(et);
				if(ti.isRNS()){
					WindowsDirHandle dirEntry = new WindowsDirHandle(entry, 
							WindowsResourceHandle.INFORMATION_ONLY, false);
					WindowsDirHandle oldHandle = 
						(WindowsDirHandle)directoryEntries.put(entry.getName(), dirEntry);
					
					if(oldHandle != null)	oldHandle.close(false);
				}
				else if(ti.isByteIO()){
					WindowsFileHandle fileEntry = new WindowsFileHandle(entry, 
						WindowsResourceHandle.OPEN,
						WindowsResourceHandle.INFORMATION_ONLY, null);
					WindowsFileHandle oldHandle = 
						(WindowsFileHandle)directoryEntries.put(entry.getName(), fileEntry);					
					
					if(oldHandle != null)	oldHandle.close(false);
				} else {
					// For non-Byte IO we treat like a file
					WindowsFileHandle fileEntry = 
						WindowsFileHandle.createNonByteIOFileHandle(entry);
					WindowsFileHandle oldHandle = 
						(WindowsFileHandle)directoryEntries.put(entry.getName(), fileEntry);
					if(oldHandle != null)	oldHandle.close(false);
				}
			}			
			setDirty(false);
		}catch(RNSPathDoesNotExistException rnse){
			//No entries
		}catch(Exception e){
			System.out.println("G-ICING:  Error reading directory: " + 
					rnsPath.pwd());
			e.printStackTrace();
		}finally{
			dirEntriesLock.writeLock().unlock();
		}	
	}
	
	public void addEntry(String name, WindowsResourceHandle handle){
		dirEntriesLock.writeLock().lock();
		{
			directoryEntries.put(name, handle);
		}
		dirEntriesLock.writeLock().unlock();		
	}
	
	public void removeEntry(String name){
		dirEntriesLock.writeLock().lock();
		{
			directoryEntries.remove(name);
		}
		dirEntriesLock.writeLock().unlock();
	}
}
