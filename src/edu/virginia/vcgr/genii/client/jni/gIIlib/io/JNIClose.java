package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

public class JNIClose extends JNILibraryBase 
{
	static private Log _logger = LogFactory.getLog(JNIClose.class);
	
	public static Boolean close(Integer handle, Boolean deleteOnClose)
	{
		_logger.trace(String.format(
			"JNIClose::close(%d, deleteOnClose = %s)", handle, deleteOnClose));
		
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		
		FilesystemHandle fsHandle = openHandles.get(handle);
		
		if(fsHandle == null)
		{
			_logger.error("G-ICING:  Invalid handle received on file close");			
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
				_logger.error("Unable to delete file handle.", fse);				
				return false;
			}
		}
	}
}