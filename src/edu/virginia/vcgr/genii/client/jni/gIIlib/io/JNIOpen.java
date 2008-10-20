package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.io.File;
import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.ResourceInformation;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.CacheManager;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsDirHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsFileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsResourceHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.localtest.TestDirHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.localtest.TestFileHandle;

public class JNIOpen extends JNILibraryBase{
	
	public static ArrayList<String> open(String fileName, Integer requestedDeposition,
			Integer desiredAccess, Boolean mustBeADirectory){		
		
		ResourceInformation information = null;
		CacheManager manager = CacheManager.getInstance();			
			
		/*-----		First three operations you must do -----*/
		if(!isValidPath(fileName))  return null;		
		fileName = cleanupPath(fileName);		
		tryToInitialize();
		/*-----				End requirement				-----*/
		
		try{
			if(mustBeADirectory){
				WindowsDirHandle dirHandle;				
				if(ENABLE_LOCAL_TEST) {
					dirHandle = new TestDirHandle(fileName, requestedDeposition, 
						desiredAccess);
				} else {				
					dirHandle = new WindowsDirHandle(fileName, requestedDeposition, 
						desiredAccess);
				}
				information = dirHandle.getCachedInformation();
				manager.addHandle(dirHandle);
			}
			else if(!mustBeADirectory && 
				(requestedDeposition == WindowsResourceHandle.CREATE ||
					requestedDeposition == WindowsResourceHandle.OPEN_IF ||
						requestedDeposition == WindowsResourceHandle.OVERWRITE_IF)){
				//It must be a file with with deposition either open_if, create, overwrite_if
				WindowsFileHandle fileHandle;
				if(ENABLE_LOCAL_TEST) {
					fileHandle = new TestFileHandle(fileName, requestedDeposition, desiredAccess);
				} else {
					fileHandle = new WindowsFileHandle(fileName, requestedDeposition, 
						desiredAccess);
				}
				information = fileHandle.getCachedInformation();
				manager.addHandle(fileHandle);
			} else{
				//We don't know the type, so let RNS figure it out (MUST EXIST)
				WindowsResourceHandle rh;
				if(ENABLE_LOCAL_TEST) {					
					File myFile = new File(JNILibraryBase.testRoot + "/" + fileName);
					if(myFile.isDirectory()) {
						rh = new TestDirHandle(fileName, requestedDeposition, desiredAccess);
					} else {
						rh = new TestFileHandle(fileName, requestedDeposition, desiredAccess);
					}	
					information = rh.getCachedInformation();
					manager.addHandle(rh);
				} else {							
					rh = WindowsResourceHandle.openResource(fileName, 
						requestedDeposition, desiredAccess);
					
					if(rh instanceof WindowsFileHandle){
						information = ((WindowsFileHandle)rh).getCachedInformation();
						manager.addHandle((WindowsFileHandle)rh);
					}else{
						information = ((WindowsDirHandle)rh).getCachedInformation();
						manager.addHandle((WindowsDirHandle)rh);
					}
				}
			}			
		}
		catch(Exception e){
			//Squelch since Windows Reports errors			
		}		
		
		//Convert for JNI
		return information == null ? null : information.convertForJNI();		
	}
}
