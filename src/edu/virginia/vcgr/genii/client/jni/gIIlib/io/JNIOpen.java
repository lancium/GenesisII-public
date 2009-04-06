package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fsii.FSFilesystem;
import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.FilesystemEntryType;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSEntryNotFoundException;
import edu.virginia.vcgr.fsii.file.OpenFlags;
import edu.virginia.vcgr.fsii.file.OpenModes;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.exception.WindowsIFSException;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.DirectoryHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

public class JNIOpen extends JNILibraryBase
{
	static private Log _logger = LogFactory.getLog(JNIOpen.class);
	
	static private String depositionToString(int requestedDeposition)
	{
		switch (requestedDeposition)
		{
			case FilesystemHandle.SUPERSEDE :
				return "SUPERSEDE";
				
			case FilesystemHandle.OPEN :
				return "OPEN";
				
			case FilesystemHandle.CREATE :
				return "CREATE";
				
			case FilesystemHandle.OPEN_IF :
				return "OPEN_IF";
				
			case FilesystemHandle.OVERWRITE :
				return "OVERWRITE";
				
			case FilesystemHandle.OVERWRITE_IF :
				return "OVERWIRTE_IF";
				
			default :
				return "<unknown>";
		}
	}
	
	static private StringBuilder addOrString(StringBuilder builder, 
		String str)
	{
		if (builder == null)
			builder = new StringBuilder(str);
		else
			builder.append(" | " + str);
		
		return builder;
	}
	
	static private String desiredAccessToString(int desiredAccess)
	{
		if (desiredAccess == 0)
			return "INFORMATION ONLY";
		
		StringBuilder builder = null;
		
		if ((desiredAccess & FilesystemHandle.FILE_READ_DATA) > 0)
			builder = addOrString(builder, "READ");
		if ((desiredAccess & FilesystemHandle.FILE_WRITE_DATA) > 0)
			builder = addOrString(builder, "WRITE");
		if ((desiredAccess & FilesystemHandle.FILE_APPEND_DATA) > 0)
			builder = addOrString(builder, "APPEND");
		if ((desiredAccess & FilesystemHandle.FILE_EXECUTE) > 0)
			builder = addOrString(builder, "EXECUTE");
		if ((desiredAccess & FilesystemHandle.DELETE) > 0)
			builder = addOrString(builder, "DELETE");
		
		return builder.toString();
	}
	
	static public ArrayList<String> open(String fileName, 
		Integer requestedDeposition, Integer desiredAccess, 
		Boolean mustBeADirectory)
	{
		String []path = convertPath(fileName);
		
		_logger.trace(String.format(
			"JNIOpen::open(%s, %s, %s, mustBeADirectory = %s)",
			UnixFilesystemPathRepresentation.INSTANCE.toString(path),
			depositionToString(requestedDeposition),
			desiredAccessToString(desiredAccess), mustBeADirectory));
		
		if (path == null)
			return null;
		
		FSFilesystem fs = getFilesystem();
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		ArrayList<String> ret;
		
		try
		{
			if (mustBeADirectory)
				ret = openDirectory(path, requestedDeposition, desiredAccess,
					fs, openHandles);
			else if (requestedDeposition == FilesystemHandle.CREATE ||
				requestedDeposition == FilesystemHandle.OPEN_IF ||
				requestedDeposition == FilesystemHandle.OVERWRITE_IF)
			{
				ret = openFile(path, requestedDeposition, desiredAccess,
					fs, openHandles);
			} else
			{
				ret = openUnknown(path, requestedDeposition, desiredAccess,
					fs, openHandles);
			}
		}
		catch (Exception e)
		{
			_logger.warn("Error reading directory.", e);
			// Squelch since Windows Reports errors
			return null;
		}
		
		_logger.trace(String.format("JNIOpen::open -- %s",
			toString(ret)));
		return ret;
	}
	
	static private ArrayList<String> openDirectory(String []path,
		int requestedDeposition, int desiredAccess,
		FSFilesystem fs, FileHandleTable<FilesystemHandle> openHandles)
			throws Exception
	{
		FilesystemStatStructure stat;
		
		switch (requestedDeposition)
		{
			case FilesystemHandle.CREATE :
				fs.mkdir(path, null);
				stat = fs.stat(path);
				break;
				
			case FilesystemHandle.OPEN :
				stat = fs.stat(path);
				break;
				
			case FilesystemHandle.OPEN_IF :
				try
				{
					stat = fs.stat(path);
				}
				catch (FSEntryNotFoundException fsenfe)
				{
					fs.mkdir(path, null);
					stat = fs.stat(path);
				}
				break;
				
			default :
				throw new WindowsIFSException(
					"Unknown type or incorrect deposition for open dir.");
		}
		
		int handle = FilesystemHandle.INVALID_HANDLE;
		
		if (requestedDeposition != FilesystemHandle.INFORMATION_ONLY)
		{
			synchronized(openHandles)
			{
				handle = openHandles.allocate(
					new DirectoryHandle(fs, path));
			}
		}
		
		ArrayList<String> ret = new ArrayList<String>(4);
		
		ret.add(Integer.toString(handle));
		ret.add("D");
		ret.add(Long.toString(stat.getSize()));
		String name = "/";
		if (path.length > 0)
			name = path[path.length - 1];
		ret.add(name);
		
		return ret;
	}
	
	static private ArrayList<String> openFile(String []path,
		int requestedDeposition, int desiredAccess,
		FSFilesystem fs, FileHandleTable<FilesystemHandle> openHandles)
			throws Exception
	{
		FilesystemStatStructure stat;
		
		OpenModes mode;
		OpenFlags flags;
		boolean isTruncate = 
			(requestedDeposition == FilesystemHandle.OVERWRITE);
		
		/* I Know that this is wrong, but append isn't working and frankly isn't
		 * used that often.
		boolean isAppend = 
			(desiredAccess & FilesystemHandle.FILE_APPEND_DATA) > 0;
		*/
		boolean isAppend = false;
			
		switch (requestedDeposition)
		{
			case FilesystemHandle.SUPERSEDE :
				// Remove from parent
				try { fs.unlink(path); } catch (Throwable cause) {}
				// Let it roll right over into the create case.
				
			case FilesystemHandle.CREATE :
				flags = new OpenFlags(true, isAppend, isTruncate, true);
				break;
				
			case FilesystemHandle.OPEN :
			case FilesystemHandle.OVERWRITE :
				flags = new OpenFlags(false, isAppend, isTruncate, false);
				break;
				
			case FilesystemHandle.OPEN_IF :
			case FilesystemHandle.OVERWRITE_IF :
				flags = new OpenFlags(true, isAppend, isTruncate, false);
				break;
			default :
				throw new WindowsIFSException("Unknown type.");	
		}
		
		boolean isWrite = (desiredAccess & FilesystemHandle.FILE_WRITE_DATA) > 0;
		isWrite = isWrite || ((desiredAccess & FilesystemHandle.FILE_APPEND_DATA) > 0);
		
		mode = isWrite ?
			OpenModes.READ_WRITE : OpenModes.READ;
		
		int handle = FilesystemHandle.INVALID_HANDLE;
		
		if (requestedDeposition != FilesystemHandle.INFORMATION_ONLY)
		{
			long fileHandle = fs.open(path, flags, mode, null);
			synchronized(openHandles)
			{
				handle = openHandles.allocate(new FileHandle(fs, path, fileHandle));
			}
		}
		
		stat = fs.stat(path);
		ArrayList<String> ret = new ArrayList<String>(4);
		
		ret.add(Integer.toString(handle));
		ret.add("F");
		ret.add(Long.toString(stat.getSize()));
		String name = "/";
		if (path.length > 0)
			name = path[path.length - 1];
		ret.add(name);
		
		
		return ret;
	}
	
	static private ArrayList<String> openUnknown(String []path,
		int requestedDeposition, int desiredAccess,
		FSFilesystem fs, FileHandleTable<FilesystemHandle> openHandles)
			throws Exception
	{
		FilesystemStatStructure stat = fs.stat(path);
		if (stat.getEntryType() == FilesystemEntryType.DIRECTORY)
			return openDirectory(path, requestedDeposition, desiredAccess, fs,
				openHandles);
		
		return openFile(path, requestedDeposition, desiredAccess, fs, 
			openHandles);
	}
}