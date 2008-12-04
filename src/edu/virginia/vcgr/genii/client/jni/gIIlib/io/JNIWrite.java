package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

public class JNIWrite extends JNILibraryBase
{
	static private Log _logger = LogFactory.getLog(JNIWrite.class);
	
	public static Integer write(Integer fileHandle, byte[] data, Long offset)
	{
		_logger.trace(String.format(
			"JNIWrite::write(%d, ..., %d)", fileHandle, offset));
		
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		
		FilesystemHandle fsHandle = openHandles.get(fileHandle);
		if(fsHandle == null || fsHandle.isDirectoryHandle())
		{
			_logger.error("G-ICING:  Invalid handle received for file write");			
			return null;
		}
			
		try
		{
			return ((FileHandle)fsHandle).write(offset, data);
		}
		catch (FSException fse)
		{
			_logger.error("Unable to write to file.", fse);
			return null;
		}
	}
	
	public static Integer truncateAppend(Integer fileHandle, byte[] data, Long offset)
	{
		_logger.trace(String.format(
			"JNIWrite::truncateAppend(%d, ..., %d)", fileHandle, offset));
		
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		
		FilesystemHandle fsHandle = openHandles.get(fileHandle);
		if(fsHandle == null || fsHandle.isDirectoryHandle())
		{
			_logger.error("G-ICING:  Invalid handle received for file truncAppend");			
			return null;
		}
			
		try
		{
			return ((FileHandle)fsHandle).truncAppend(offset, data);
		}
		catch (FSException fse)
		{
			_logger.error("Unable to write to file.", fse);
			return null;
		}
	}
}