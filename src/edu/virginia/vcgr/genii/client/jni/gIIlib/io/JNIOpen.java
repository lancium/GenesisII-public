package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.ResourceInformation;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.CacheManager;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsDirHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsFileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsResourceHandle;

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
				//Must be a directory
				WindowsDirHandle dirHandle = new WindowsDirHandle(fileName, requestedDeposition, 
						desiredAccess);
				information = dirHandle.getCachedInformation();
				manager.addHandle(dirHandle);
			}
			else if(!mustBeADirectory && 
				(requestedDeposition == WindowsResourceHandle.CREATE ||
					requestedDeposition == WindowsResourceHandle.OPEN_IF ||
						requestedDeposition == WindowsResourceHandle.OVERWRITE_IF)){						
			
				//It must be a file with with deposition either open_if, create, overwrite_if
				WindowsFileHandle fileHandle = new WindowsFileHandle(fileName, requestedDeposition, 
						desiredAccess);
				information = fileHandle.getCachedInformation();
				manager.addHandle(fileHandle);
			}else{
				//We don't know the type, so let RNS figure it out
				WindowsResourceHandle rh = WindowsResourceHandle.openResource(fileName, 
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
		catch(Exception e){
			e.printStackTrace();									
		}		
		
		//Convert for JNI
		return information == null ? null : information.convertForJNI();		
	}
}
