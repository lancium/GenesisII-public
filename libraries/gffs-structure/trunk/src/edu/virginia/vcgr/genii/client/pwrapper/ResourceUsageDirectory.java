package edu.virginia.vcgr.genii.client.pwrapper;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.io.FileSystemUtils;

public class ResourceUsageDirectory
{
	private File _directory;
	private File _accountingDir;
	private File _rusage;
	private File _finishedDir;
	private File _archiveDir;
	
	synchronized File checkAccountingDir(File directory) throws IOException
	{

		// We create the sharedDir/Accounting dir if it is not there.
		String parentPath=directory.getParent();
		File actDir = new File(parentPath+"/Accounting");
		
		if (!actDir.exists()) {
			actDir.mkdirs();
			// 2020-05-28 by ASG -- Add Accounting/finished and Accounting/archive
			File finishedDir = new File(actDir.getAbsolutePath() + "/finished");
			File archiveDir = new File(actDir.getAbsolutePath() + "/archive");
			// set permissions next
			if (OperatingSystemType.isWindows()) {
				actDir.setWritable(true, false);
				finishedDir.setWritable(true, false);
				archiveDir.setWritable(true, false);
			}
			else {
				FileSystemUtils.chmod(actDir.getAbsolutePath(), FileSystemUtils.MODE_USER_READ | FileSystemUtils.MODE_USER_WRITE
						| FileSystemUtils.MODE_USER_EXECUTE| FileSystemUtils.MODE_GROUP_EXECUTE);
				FileSystemUtils.chmod(finishedDir.getAbsolutePath(), FileSystemUtils.MODE_USER_READ | FileSystemUtils.MODE_USER_WRITE
						| FileSystemUtils.MODE_USER_EXECUTE| FileSystemUtils.MODE_GROUP_EXECUTE);
				FileSystemUtils.chmod(archiveDir.getAbsolutePath(), FileSystemUtils.MODE_USER_READ | FileSystemUtils.MODE_USER_WRITE
						| FileSystemUtils.MODE_USER_EXECUTE| FileSystemUtils.MODE_GROUP_EXECUTE);
			}
			_finishedDir=finishedDir;
			_archiveDir=archiveDir;
		}
		if (!actDir.exists())
			throw new IOException(String.format("Unable to create directory \"%s\".", actDir));
		
		return actDir;

	}

	public ResourceUsageDirectory(File directory) throws ProcessWrapperException
	{
		// 2020-04-16 - ASG during the coronovirus
		// Now we temporarily need to accounting files while we switch accounting mechanisms.
		try {
			File actDir=checkAccountingDir(directory);  // Creates ..../shared/Accounting if it is not there
			File jobActDir = new File(actDir.getAbsolutePath()+ "/" + directory.getName());
			jobActDir.mkdir();
			if (!jobActDir.exists())
				throw new ProcessWrapperException(String.format("Unable to create directory \"%s\".", jobActDir));		
			// set permissions next
			// jobActDir is now .../Accounting/<jobID>
			if (OperatingSystemType.isWindows())
				jobActDir.setWritable(true, false);
			else
				FileSystemUtils.chmod(jobActDir.getAbsolutePath(), FileSystemUtils.MODE_USER_READ | FileSystemUtils.MODE_USER_WRITE
						| FileSystemUtils.MODE_USER_EXECUTE| FileSystemUtils.MODE_GROUP_EXECUTE|FileSystemUtils.MODE_GROUP_READ | FileSystemUtils.MODE_GROUP_WRITE);
			_accountingDir=jobActDir;
			// End of updates to create accounting directory.
			if (!directory.exists())
				directory.mkdirs();

			if (!directory.exists())
				throw new ProcessWrapperException(String.format("Unable to create directory \"%s\".", directory));

			if (!directory.isDirectory())
				throw new ProcessWrapperException(String.format("Path %s does not refer to a directory.", directory));
			// directory is now .../shared/<jobID>
			if (OperatingSystemType.isWindows())
				directory.setWritable(true, false);
			else
				FileSystemUtils.chmod(directory.getAbsolutePath(), FileSystemUtils.MODE_USER_READ | FileSystemUtils.MODE_USER_WRITE
						| FileSystemUtils.MODE_USER_EXECUTE| FileSystemUtils.MODE_GROUP_EXECUTE|FileSystemUtils.MODE_GROUP_READ | FileSystemUtils.MODE_GROUP_WRITE |
						FileSystemUtils.MODE_WORLD_EXECUTE | FileSystemUtils.MODE_WORLD_READ );

		} catch (IOException e){
			throw new ProcessWrapperException(String.format("Unable to create rusage file in directory %s.", _directory), e);
		}
		_directory = directory;
	}
	
	public File getAcctDir() {
		return _accountingDir;
	}
	
	public File getfinishedDir() {
		return _finishedDir;
	}
	
	public File getarchiveDir() {
		return _archiveDir;
	}
	
	public File getJWD() {
		return _directory;
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
			
			_rusage= tempFile;
			return tempFile;
		} catch (IOException ioe) {
			throw new ProcessWrapperException(String.format("Unable to create rusage file in directory %s.", _directory), ioe);
		}
	}
	synchronized public File getResourceUsageFile() throws ProcessWrapperException
	{
		return _rusage;
	}
}
