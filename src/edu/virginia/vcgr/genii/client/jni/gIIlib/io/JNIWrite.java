package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

public class JNIWrite extends JNILibraryBase
{
	public static Integer write(Integer fileHandle, byte[] data, Long offset)
	{
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		
		FilesystemHandle fsHandle = openHandles.get(fileHandle);
		if(fsHandle == null || fsHandle.isDirectoryHandle())
		{
			System.err.println("G-ICING:  Invalid handle received for file write");			
			return null;
		}
			
		try
		{
			return ((FileHandle)fsHandle).write(offset, data);
		}
		catch (FSException fse)
		{
			System.err.println("Unable to write to file.");
			fse.printStackTrace(System.err);
			
			return null;
		}
	}
	
	public static Integer truncateAppend(Integer fileHandle, byte[] data, Long offset)
	{
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		
		FilesystemHandle fsHandle = openHandles.get(fileHandle);
		if(fsHandle == null || fsHandle.isDirectoryHandle())
		{
			System.err.println("G-ICING:  Invalid handle received for file truncAppend");			
			return null;
		}
			
		try
		{
			return ((FileHandle)fsHandle).truncAppend(offset, data);
		}
		catch (FSException fse)
		{
			System.err.println("Unable to write to file.");
			fse.printStackTrace(System.err);
			
			return null;
		}
	}
}