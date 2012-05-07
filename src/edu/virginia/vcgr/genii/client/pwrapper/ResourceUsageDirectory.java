package edu.virginia.vcgr.genii.client.pwrapper;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.io.FileSystemUtils;

public class ResourceUsageDirectory
{
	private File _directory;
	
	public ResourceUsageDirectory(File directory) throws ProcessWrapperException
	{
		if (!directory.exists())
			directory.mkdirs();
		
		if (!directory.exists())
			throw new ProcessWrapperException(String.format(
				"Unable to create directory \"%s\".", directory));
		
		if (!directory.isDirectory())
			throw new ProcessWrapperException(String.format(
				"Path %s does not refer to a directory.", directory));
		
		_directory = directory;
	}
	
	synchronized public File getNewResourceUsageFile()
		throws ProcessWrapperException
	{
		try
		{
			File tempFile = File.createTempFile("rusage-", ".xml", _directory);
			
			if (OperatingSystemType.getCurrent().isWindows())
				tempFile.setWritable(true, false);
			else 
				FileSystemUtils.chmod(tempFile.getAbsolutePath(),
						FileSystemUtils.MODE_USER_READ |
						FileSystemUtils.MODE_USER_WRITE |
						FileSystemUtils.MODE_GROUP_READ |
						FileSystemUtils.MODE_GROUP_WRITE 
						);
			
			return tempFile;
		}
		catch (IOException ioe)
		{
			throw new ProcessWrapperException(String.format(
				"Unable to create rusage file in directory %s.",
				_directory), ioe);
		}
	}
}