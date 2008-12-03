package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

public class JNIRename extends JNILibraryBase 
{
	public static boolean rename(Integer fileHandle, String destination)
	{
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		
		FilesystemHandle fsHandle = openHandles.get(fileHandle);
		if(fsHandle == null)
		{
			System.err.println("G-ICING:  Invalid handle received for rename.");			
			return false;
		}
		
		return fsHandle.renameTo(convertPath(destination));
	}	
}