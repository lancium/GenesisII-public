package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.util.ArrayList;

import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.FilesystemEntryType;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSEntryNotFoundException;
import edu.virginia.vcgr.fsii.file.OpenFlags;
import edu.virginia.vcgr.fsii.file.OpenModes;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.exception.WindowsIFSException;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.DirectoryHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

public class JNIOpen extends JNILibraryBase
{
	static public ArrayList<String> open(String fileName, 
		Integer requestedDeposition, Integer desiredAccess, 
		Boolean mustBeADirectory)
	{
		String []path = convertPath(fileName);
		if (path == null)
			return null;
		
		GenesisIIFilesystem fs = getFilesystem();
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		
		try
		{
			if (mustBeADirectory)
				return openDirectory(path, requestedDeposition, desiredAccess,
					fs, openHandles);
			else if (requestedDeposition == FilesystemHandle.CREATE ||
				requestedDeposition == FilesystemHandle.OPEN_IF ||
				requestedDeposition == FilesystemHandle.OVERWRITE_IF)
			{
				return openFile(path, requestedDeposition, desiredAccess,
					fs, openHandles);
			} else
			{
				return openUnknown(path, requestedDeposition, desiredAccess,
					fs, openHandles);
			}
		}
		catch (Exception e)
		{
			// Squelch since Windows Reports errors
			return null;
		}
	}
	
	static private ArrayList<String> openDirectory(String []path,
		int requestedDeposition, int desiredAccess,
		GenesisIIFilesystem fs, FileHandleTable<FilesystemHandle> openHandles)
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
		
		int handle;
		synchronized(openHandles)
		{
			handle = openHandles.allocate(new DirectoryHandle(fs, path));
		}
		
		ArrayList<String> ret = new ArrayList<String>(4);
		
		ret.set(0, Integer.toString(handle));
		ret.set(1, "D");
		ret.set(2, Long.toString(stat.getSize()));
		ret.set(3, path[path.length - 1]);
		
		return ret;
	}
	
	static private ArrayList<String> openFile(String []path,
		int requestedDeposition, int desiredAccess,
		GenesisIIFilesystem fs, FileHandleTable<FilesystemHandle> openHandles)
			throws Exception
	{
		FilesystemStatStructure stat;
		
		OpenModes mode;
		OpenFlags flags;
		boolean isTruncate = 
			(requestedDeposition == FilesystemHandle.OVERWRITE);
		boolean isAppend = 
			(desiredAccess & FilesystemHandle.FILE_APPEND_DATA) > 0;
			
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
		
		mode = (desiredAccess & FilesystemHandle.FILE_WRITE_DATA) > 0 ?
			OpenModes.READ_WRITE : OpenModes.READ;
		
		long fileHandle = fs.open(path, flags, mode, null);
		int handle;
		synchronized(openHandles)
		{
			handle = openHandles.allocate(new FileHandle(fs, path, fileHandle));
		}
		
		stat = fs.stat(path);
		ArrayList<String> ret = new ArrayList<String>(4);
		
		ret.set(0, Integer.toString(handle));
		ret.set(1, "F");
		ret.set(2, Long.toString(stat.getSize()));
		ret.set(3, path[path.length - 1]);
		
		return ret;
	}
	
	static private ArrayList<String> openUnknown(String []path,
		int requestedDeposition, int desiredAccess,
		GenesisIIFilesystem fs, FileHandleTable<FilesystemHandle> openHandles)
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