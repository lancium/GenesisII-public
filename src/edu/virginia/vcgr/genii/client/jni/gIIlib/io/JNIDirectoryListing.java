package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.morgan.util.file.FilePattern;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.FilesystemEntryType;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.DirectoryHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

public class JNIDirectoryListing extends JNILibraryBase
{
	public static ArrayList<String> getDirectoryListing(Integer handle, 
		String target) 
	{
		FileHandleTable<FilesystemHandle> openHandles = openHandles();
		FilesystemHandle fsHandle;
		DirectoryHandle dirHandle;
		
		fsHandle = openHandles.get(handle);
		if(fsHandle == null || !fsHandle.isDirectoryHandle())
		{
			System.err.println(
				"G-ICING:  Invalid handle received for directory listing");			
			return null;
		}
		
		dirHandle = (DirectoryHandle)fsHandle;
		
		Pattern pat = null;
		if (target != null)
			pat = FilePattern.compile(target);
		
		Iterable<FilesystemStatStructure> entries = null;
		
		try
		{
			entries = dirHandle.listEntries();
		
			ArrayList<String> ret = new ArrayList<String>();
			for (FilesystemStatStructure stat : entries)
			{
				if (pat != null && !(pat.matcher(stat.getName()).matches()))
					continue;
				
				ret.add(Integer.toString(FilesystemHandle.INVALID_HANDLE));
				ret.add( 
					(stat.getEntryType() == FilesystemEntryType.DIRECTORY) ?
						"D" : "F");
				ret.add(Long.toString(stat.getSize()));
				ret.add(stat.getName());
			}
			
			return ret;
		}
		catch (FSException fse)
		{
			System.err.println("Unable to get directory listing.");
			fse.printStackTrace(System.err);
			
			return null;
		}
		finally
		{
			if (entries != null && entries instanceof Closeable)
				StreamUtils.close((Closeable)entries);
		}
	}
}