package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.CacheManager;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsDirHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsFileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsResourceHandle;

public class JNIRead {
	public static byte[] read(Integer fileHandle, Long offset, Integer length){		
		CacheManager manager = CacheManager.getInstance();		
		WindowsResourceHandle resourceHandle = manager.getHandle(fileHandle);							
		
		if(resourceHandle == null || resourceHandle instanceof WindowsDirHandle){
			System.out.println("G-ICING:  Invalid handle received for file read");			
			return null;
		}		
		WindowsFileHandle fh = (WindowsFileHandle) resourceHandle;		
		return fh.read(offset, length);						
	}
}