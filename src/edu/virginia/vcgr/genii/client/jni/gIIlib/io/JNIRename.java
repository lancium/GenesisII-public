package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

public class JNIRename extends JNILibraryBase
{
	static private Log _logger = LogFactory.getLog(JNIRename.class);

	public static boolean rename(Integer fileHandle, String destination)
	{
		if (_logger.isTraceEnabled())
			_logger.trace(String.format("JNIRename::rename(%d, %s)", fileHandle, destination));

		FileHandleTable<FilesystemHandle> openHandles = openHandles();

		FilesystemHandle fsHandle = openHandles.get(fileHandle);
		if (fsHandle == null) {
			_logger.error("G-ICING:  Invalid handle received for rename.");
			return false;
		}

		return fsHandle.renameTo(convertPath(destination));
	}
}