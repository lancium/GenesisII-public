package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

public class JNIRead extends JNILibraryBase 
{
	static private Log _logger = LogFactory.getLog(JNIRead.class);
	
	public static byte[] read(Integer fileHandle, Long offset, Integer length)
	{
		_logger.trace(String.format(
			"JNIRead::read(%d, %d, %d)",
			fileHandle, offset, length));
		
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		
		FilesystemHandle fsHandle = openHandles.get(fileHandle);
		if(fsHandle == null || fsHandle.isDirectoryHandle())
		{
			_logger.error("G-ICING:  Invalid handle received for file read");			
			return null;
		}
			
		try
		{
			return ((FileHandle)fsHandle).read(offset, length);
		}
		catch (FSException fse)
		{
			_logger.error("Unable to read from file.", fse);
			return null;
		}
	}
}