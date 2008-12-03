package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

public class JNIRead extends JNILibraryBase 
{
	public static byte[] read(Integer fileHandle, Long offset, Integer length)
	{
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		
		FilesystemHandle fsHandle = openHandles.get(fileHandle);
		if(fsHandle == null || fsHandle.isDirectoryHandle())
		{
			System.err.println("G-ICING:  Invalid handle received for file read");			
			return null;
		}
			
		try
		{
			return ((FileHandle)fsHandle).read(offset, length);
		}
		catch (FSException fse)
		{
			System.err.println("Unable to read from file.");
			fse.printStackTrace(System.err);
			
			return null;
		}
	}
}