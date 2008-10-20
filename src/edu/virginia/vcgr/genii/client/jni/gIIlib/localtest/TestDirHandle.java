package edu.virginia.vcgr.genii.client.jni.gIIlib.localtest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.ResourceInformation;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.CacheManager;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsDirHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsResourceHandle;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;


public class TestDirHandle extends WindowsDirHandle {
	
	private File myDirectory = null;
	private String fileName;
	
	public TestDirHandle(String fileName, Integer requestedDeposition,
			Integer desiredAccess) throws Exception {
		fileHandle = nextHandle();
		this.fileName = fileName;
		myDirectory = new File(JNILibraryBase.testRoot + "/" + fileName); 		
		
		switch(requestedDeposition){
			case CREATE:
				if(myDirectory.exists()) {
					throw new IOException("Directory already exists");
				}
				myDirectory.mkdir();
				break;
			case OPEN_IF:
				if(!myDirectory.exists()) {
					myDirectory.mkdir();
				}
				break;
			case OPEN:
				break;
			default:
				throw new IOException("Unknown type or incorrect desposition for open dir");		
		}			
		if(!myDirectory.exists() || !myDirectory.isDirectory()){
			throw new IOException("Not a valid directory");
		}
	}

	public TestDirHandle(RNSPath filePath, Integer desiredAccess,
			boolean refreshDirectory) throws IOException, RNSException {
		throw new IOException("TestDir does not support this constructor");
	}
	
	public ResourceInformation getCachedInformation() {
		Date lastModified = new Date(myDirectory.lastModified());		
		if(fileName.trim().equals("/")) {
			return new ResourceInformation(true, "/", fileHandle, lastModified,
				lastModified, lastModified, 0);
		} else {
			return new ResourceInformation(true, myDirectory.getName(), fileHandle, 
				lastModified, lastModified, lastModified, 0);
		}
	}
	
	public ArrayList<ResourceInformation> getEntries(String target){
		ArrayList<ResourceInformation>toReturn = new ArrayList<ResourceInformation>();
		File[] files = myDirectory.listFiles();
		
		for(File f : files) {			
			if(f.getName().matches(target)) {				
				if(f.isDirectory()) {
					try {
						toReturn.add(new TestDirHandle(fileName + "/" + f.getName(), WindowsResourceHandle.OPEN, 
								0).getCachedInformation());
					} catch(Exception e) {
						e.printStackTrace();
					}				
				} else {
					try {					
						toReturn.add(new TestFileHandle(fileName + "/" + f.getName(), WindowsResourceHandle.OPEN, 
								0).getCachedInformation());
					} catch (Exception e){
						e.printStackTrace();
					}
				}		
			}
		}
		return toReturn;
	}
	public boolean rename(String newPath) {
		File newDirectory = new File(newPath);
		if(myDirectory.renameTo(newDirectory)) {
			myDirectory = newDirectory;
			return true;
		}
		return false;
	}
	
	public void close(boolean deleteOnClose) {
		if(deleteOnClose) {
			myDirectory.delete();
		}
		CacheManager.getInstance().removeHandle(fileHandle);
	}
}