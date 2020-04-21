package edu.virginia.vcgr.genii.client.pwrapper;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.io.FileSystemUtils;

public class ResourceUsageDirectory
{
	private File _directory;
	private File _accountingDir;

	public ResourceUsageDirectory(File directory) throws ProcessWrapperException
	{
		// 2020-04-16 - ASG during the coronovirus
		// Now we temporarily need to accounting files while we switch accounting mechanisms.
		String parentPath=directory.getParent();
		File actDir = new File(parentPath+"/Accounting");
		if (!actDir.exists()) {
			actDir.mkdirs();
		}
		if (!actDir.exists())
			throw new ProcessWrapperException(String.format("Unable to create directory \"%s\".", actDir));
		File jobActDir = new File(actDir.getAbsolutePath()+ "/" + directory.getName());
		jobActDir.mkdir();
		if (!jobActDir.exists())
			throw new ProcessWrapperException(String.format("Unable to create directory \"%s\".", jobActDir));
		// End of updates to create both directories.
		_accountingDir=jobActDir;
		if (!directory.exists())
			directory.mkdirs();

		if (!directory.exists())
			throw new ProcessWrapperException(String.format("Unable to create directory \"%s\".", directory));

		if (!directory.isDirectory())
			throw new ProcessWrapperException(String.format("Path %s does not refer to a directory.", directory));

		_directory = directory;
	}
	
	public File getAcctDir() {
		return _accountingDir;
	}

	synchronized public File getNewResourceUsageFile() throws ProcessWrapperException
	{
		try {
			// 2019-06-05 ASG. Changing the nameing scheme for resource usage files to be constant, without 
			// any random bits. That way we can always stage it out.
			// File tempFile = File.createTempFile("rusage-", ".xml", _directory);
			File tempFile = new File(_directory,"rusage.xml");
			tempFile.createNewFile();

			if (OperatingSystemType.isWindows())
				tempFile.setWritable(true, false);
			else
				FileSystemUtils.chmod(tempFile.getAbsolutePath(), FileSystemUtils.MODE_USER_READ | FileSystemUtils.MODE_USER_WRITE
					| FileSystemUtils.MODE_GROUP_READ | FileSystemUtils.MODE_GROUP_WRITE);

			return tempFile;
		} catch (IOException ioe) {
			throw new ProcessWrapperException(String.format("Unable to create rusage file in directory %s.", _directory), ioe);
		}
	}
	synchronized public File getResourceUsageFile() throws ProcessWrapperException
	{

		// 2019-06-05 ASG. Changing the nameing scheme for resource usage files to be constant, without 
		// any random bits. That way we can always stage it out.
		// File tempFile = File.createTempFile("rusage-", ".xml", _directory);
		File tempFile = new File(_directory,"rusage.xml");
		return tempFile;
	}
}