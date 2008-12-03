package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

public class JNIClose extends JNILibraryBase 
{
	public static Boolean close(Integer handle, Boolean deleteOnClose)
	{		
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		
		FilesystemHandle fsHandle = openHandles.get(handle);
		
		if(fsHandle == null)
		{
			System.err.println("G-ICING:  Invalid handle received on file close");			
			return false;
		} else
		{
			try
			{
				if (deleteOnClose)
					fsHandle.delete();
				openHandles.release(handle);
			
				return true;
			}
			catch (FSException fse)
			{
				System.err.println("Unable to delete file handle.");
				fse.printStackTrace(System.err);
				
				return false;
			}
		}
	}
}
