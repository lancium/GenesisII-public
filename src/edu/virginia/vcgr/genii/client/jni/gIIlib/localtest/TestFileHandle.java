package edu.virginia.vcgr.genii.client.jni.gIIlib.localtest;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.ResourceInformation;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.CacheManager;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.CachedDir;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsFileHandle;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class TestFileHandle extends WindowsFileHandle {
	
	String fileName;
	File myFile;

	public TestFileHandle(String fileName, Integer requestedDeposition,
			Integer desiredAccess) throws Exception {
		fileHandle = nextHandle();
		this.fileName = fileName;
		myFile = new File(JNILibraryBase.testRoot + "/" + fileName);
		
		switch(requestedDeposition){
			case SUPERSEDE:
				if(myFile.exists()){
					myFile.delete();						
				}
				// Fall through is on purpose
			case CREATE:
				if(!myFile.exists()){
					myFile.createNewFile();
				} else {
					throw new IOException("File cannot be created over existing file");
				}
				break;
			case OVERWRITE_IF:
				if(!myFile.exists()) {
					myFile.createNewFile();
				}
				//	Fall through on purpose
			case OVERWRITE:
				RandomAccessFile raf = new RandomAccessFile(myFile, "rw");
				raf.setLength(0);
				raf.close();
				break;
			case OPEN_IF:
				if(!myFile.exists()) {
					myFile.createNewFile();
				}
				// Fall through on purpose
			case OPEN:
				break;
			default:
				throw new IOException("Unknown type or incorrect desposition for open file");
		}
		
		if(!myFile.exists() || myFile.isDirectory()){
			throw new IOException("Not a valid file");
		}
	}

	public TestFileHandle(RNSPath filePath, Integer requestedDeposition,
			Integer desiredAccess, CachedDir cachedParent) throws RNSException,
			IOException {
		throw new IOException("TestDir does not support this constructor");
	}
	
	public ResourceInformation getCachedInformation() {
		Date lastModified = new Date(myFile.lastModified());
		return new ResourceInformation(false, myFile.getName(), fileHandle, 
				lastModified, lastModified, lastModified, myFile.length());
	}
	
	public boolean rename(String newPath) {
		File newFile = new File(newPath);
		if(myFile.renameTo(newFile)) {
			myFile = newFile;
			return true;
		}
		return false;
	}
	
	public void close(boolean deleteOnClose) {
		if(deleteOnClose) {
			myFile.delete();
		}
		CacheManager.getInstance().removeHandle(fileHandle);
	}
	
	public byte[] read(Long offset, Integer length){
		byte[] toRead = new byte[length];
		try {
			RandomAccessFile raf = new RandomAccessFile(myFile, "r");
			raf.seek(offset);
			raf.readFully(toRead);
			raf.close();			
		} catch(Exception e) {
			toRead = null;
		}
		return toRead;
	}
	
	public int write(byte[] data, Long offset){		
		try {
			RandomAccessFile raf = new RandomAccessFile(myFile, "rw");
			raf.seek(offset);
			raf.write(data);
			raf.close();						
		} catch(Exception e) {
			e.printStackTrace();
			return 0;
		}		
		return data.length;
	}
	
	public int truncateAppend(byte[] data, Long offset){
		try {
			RandomAccessFile raf = new RandomAccessFile(myFile, "rw");
			raf.setLength(offset + 1);
			raf.seek(offset);
			raf.write(data);
			raf.close();
		} catch(Exception e) {
			e.printStackTrace();
			return 0;
		}		
		return data.length;						
	}
}
