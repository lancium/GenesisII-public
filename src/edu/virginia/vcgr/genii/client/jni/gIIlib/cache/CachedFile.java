package edu.virginia.vcgr.genii.client.jni.gIIlib.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.jni.gIIlib.ResourceInformation;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.IFSFile;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.RandomByteIOFileDescriptor;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.StreamableByteIOFileDescriptor;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsFileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsResourceHandle;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

/** 
 * This class is responsible for caching all information obtained from RNS for a file
 */
public class CachedFile extends CachedResource {
	
	private ReentrantReadWriteLock informationLock = new ReentrantReadWriteLock(true);

	private int fileSize = 0;
	
	private boolean isReadable;
	
	private boolean isWritable;
	
	private boolean isAppendable;
	
	//A reference to the ByteIO handler for this resource
	private IFSFile byteIOHandler;

	//A list of handles that are referencing me
	private ArrayList <WindowsFileHandle> myHandles = new ArrayList<WindowsFileHandle>();	
	
	public CachedFile(RNSPath filePath, int desiredAccess,
			boolean truncate) throws RNSException, IOException{
		super();
		
		boolean isAppend = (desiredAccess & WindowsResourceHandle.FILE_APPEND_DATA) > 0;
		boolean isRead = (desiredAccess & WindowsResourceHandle.FILE_READ_DATA) > 0;
		boolean isWrite = (desiredAccess & WindowsResourceHandle.FILE_WRITE_DATA) > 0;		
		
		rnsPath = filePath;
		
		EndpointReferenceType epr = rnsPath.getEndpoint();						
		TypeInformation typeInfo = new TypeInformation(epr);
		
		lastAccessedTime = typeInfo.getByteIOAccessTime();
		lastModifiedTime = typeInfo.getByteIOModificationTime();
		createTime = typeInfo.getByteIOCreateTime();
		fileSize = (int)typeInfo.getByteIOSize();			
		isDirectory = false;
		
		isReadable = isRead;
		isWritable = isWrite;
		isAppendable = isAppend;
		
		//If ONLY information, then don't connect to ByteIO
		if(desiredAccess != WindowsResourceHandle.INFORMATION_ONLY){
			reconnectToEpr(truncate);
		}
	}
	
	/**
	 * Attach file handle to this cached file
	 */
	synchronized public void attach(WindowsFileHandle fh, int desiredAccess, 
			boolean truncate) throws RNSException, IOException {
		boolean isAppend = (desiredAccess & WindowsResourceHandle.FILE_APPEND_DATA) > 0;
		boolean isRead = (desiredAccess & WindowsResourceHandle.FILE_READ_DATA) > 0;
		boolean isWrite = (desiredAccess & WindowsResourceHandle.FILE_WRITE_DATA) > 0;
		
		boolean newRead = isRead || isReadable;
		boolean newWrite = isWrite || isWritable;
		boolean newAppend = isAppend || isAppendable;
		
		
		//If change detected (need superset of current permissions)
		if((newRead && !isReadable) || (newWrite && !isWritable) || 
				(newAppend && !isAppendable)){
										
			isReadable = newRead;
			isWritable = newWrite;
			isAppendable = newAppend;
						
			reconnectToEpr(truncate);		
		}	
		
		synchronized(myHandles){
			myHandles.add(fh);
		}
	}
	
	public void detatch(WindowsFileHandle fh){
		synchronized(myHandles){
			myHandles.remove(fh);
		}
	}
	
	/** 
	 * Invalidates File Handles that are referring to this entry
	 * @param warnOnValidate
	 */
	public void invalidate(boolean warnOnValidate){
		synchronized(myHandles){
			invalidated = true;
			if(warnOnValidate && myHandles.size() > 0){
				System.out.println("Invalidating " + myHandles.size() + " handles for " +
						rnsPath.getName());
			}
			for(WindowsFileHandle fh : myHandles)
			{			
				fh.invalidate();
			}
			myHandles.clear();
		}
	}		
	
	public ResourceInformation getCachedInformation(int fileHandle){
		ResourceInformation info;
		
		if(isDirty()){								
			refreshInformation();											
		}				
		
		informationLock.readLock().lock();
		info = new ResourceInformation(false, rnsPath.getName(), 
			fileHandle, lastAccessedTime, lastModifiedTime, createTime, fileSize);
		informationLock.readLock().unlock();
		
		return info;
	}
	
	synchronized public byte[] read(int offset, int length){
		byte[] toReturn = null;		
		try{
			if(isReadable && length > 0){				
				byteIOHandler.lseek64(offset);							
				toReturn = byteIOHandler.read(length);
			}			
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return toReturn;		
	}
	
	synchronized public int write(byte []data, long offset){
		int bytesWritten = 0;
		try{
			if(isWritable && data.length > 0){				
				byteIOHandler.lseek64(offset);			
				bytesWritten = byteIOHandler.write(data);
				setDirty(true);
			}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}		
		return bytesWritten;		
	}	
	
	synchronized public int truncateAppend(byte[] data, Integer offset){
		if(isWritable){		
			try{
				reconnectToEpr(true);
				return write(data, offset);
			}catch(Exception e){
				e.printStackTrace();
				return 0;
			} 
		}
		return 0;				
	}
	
	/* Creates ByteIO Connection */
	public void reconnectToEpr(boolean truncate) 
		throws IOException, RNSException{				
		
		if(byteIOHandler != null){
			byteIOHandler.close();
		}
		
		EndpointReferenceType epr = rnsPath.getEndpoint();				
		TypeInformation typeInfo = new TypeInformation(epr);				
		
		if(byteIOHandler != null){
			byteIOHandler.close();
		}
		
		//Check the type of ByteIO and create it according to the options specified				
		if (typeInfo.isRByteIO()){	
			byteIOHandler = new RandomByteIOFileDescriptor(
					rnsPath, epr, isReadable, isWritable, isAppendable, truncate);
																										
		} else if (typeInfo.isSByteIO()){
			byteIOHandler = new StreamableByteIOFileDescriptor(rnsPath,
					epr, isReadable, isWritable, isAppendable);
		} else if (typeInfo.isSByteIOFactory())	{
			throw new RNSException("SByteIOFactory is unimplemented.");
		} else
		{
			//TODO: Implement this (SEE Mark's code)
			throw new RNSException("The path refers to an " +
					"object that isn't a file.");
		}					
	}
	
	/* Refreshes RNS Meta information */
	private void refreshInformation(){
		try{
			informationLock.writeLock().lock();
			EndpointReferenceType epr = rnsPath.getEndpoint();				
			TypeInformation typeInfo = new TypeInformation(epr);		
			lastAccessedTime = typeInfo.getByteIOAccessTime();
			lastModifiedTime = typeInfo.getByteIOModificationTime();
			createTime = typeInfo.getByteIOCreateTime();
			fileSize = (int)typeInfo.getByteIOSize();
		}catch(RNSPathDoesNotExistException rpdnee){
			System.err.println("G-ICING:  Error refresing information");
			rpdnee.printStackTrace();
		}finally{
			setDirty(false);
			informationLock.writeLock().unlock();			
		}
	}	
}
