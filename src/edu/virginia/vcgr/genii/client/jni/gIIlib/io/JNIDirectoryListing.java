package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.ResourceInformation;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.CacheManager;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsDirHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsFileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsResourceHandle;

public class JNIDirectoryListing extends JNILibraryBase
{
	public static ArrayList<String> getDirectoryListing(Integer handle, String target) {
		CacheManager manager = CacheManager.getInstance();		
		WindowsResourceHandle resourceHandle = manager.getHandle(handle);							
		
		if(resourceHandle == null || resourceHandle instanceof WindowsFileHandle){
			System.out.println("G-ICING:  Invalid handle received for directory listing");			
			return null;
		}
		
		if(target != null) {		
			// Parenthesis are legal in file names
			target = target.replace("(", "\\(");
			target = target.replace(")", "\\)");
			
			// .'s are legal
			target = target.replace(".", "\\.");
		}
		
		//Default .* behavior or replace all * with .*
		target = ((target=="" || target == null) ? ".*" : target.replace("*", ".*"));
		
		WindowsDirHandle dirHandle = (WindowsDirHandle) resourceHandle;
		ArrayList<ResourceInformation> entries = dirHandle.getEntries(target);
		
		//Convert for JNI
		ArrayList<String> toReturn = new ArrayList<String>();
		
		for(ResourceInformation info : entries){
			toReturn.addAll(info.convertForJNI());
		}
		return toReturn;
	}
}