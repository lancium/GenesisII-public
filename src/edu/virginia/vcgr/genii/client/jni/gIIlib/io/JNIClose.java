package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.CacheManager;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsResourceHandle;


public class JNIClose {
	public static Boolean close(Integer handle, Boolean deleteOnClose){		
									
		CacheManager manager = CacheManager.getInstance();
		
		WindowsResourceHandle resourceHandle = manager.getHandle(handle);							
		
		if(resourceHandle == null){
			System.out.println("G-ICING:  Invalid handle received on file close");			
			return false;
		}else{
			resourceHandle.close(deleteOnClose);			
			manager.removeHandle(handle);
			return true;
		}
	}
}
