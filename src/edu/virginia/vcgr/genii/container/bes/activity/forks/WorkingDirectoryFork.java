package edu.virginia.vcgr.genii.container.bes.activity.forks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.bes.activity.resource.IBESActivityResource;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.rfork.AbstractRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;

public class WorkingDirectoryFork extends AbstractRNSResourceFork
{
	static public final String FORK_BASE_PATH_NAME = "working-dir";
	static public final String FORK_BASE_PATH = 
		"/" + FORK_BASE_PATH_NAME;
	
	private File getTargetDirectory() throws IOException
	{
		File ret;
		String relativePath = getForkPath();
		if (!relativePath.startsWith(FORK_BASE_PATH))
			throw new FileNotFoundException(String.format(
				"Invalid fork path specified (%s).", relativePath));
		relativePath = relativePath.substring(FORK_BASE_PATH.length());
		
		if (relativePath.length() > 0 && relativePath.startsWith("/"))
			relativePath = relativePath.substring(1);
		
		IBESActivityResource resource = 
			(IBESActivityResource)getService().getResourceKey().dereference();
		
		BESActivity activity = resource.findActivity();
		BESWorkingDirectory workingDir = activity.getActivityCWD();
		if (relativePath.length() == 0)
			ret = workingDir.getWorkingDirectory();
		else
			ret = new File(workingDir.getWorkingDirectory(), relativePath);
		
		if (!ret.exists())
			throw new FileNotFoundException(String.format(
				"Couldn't find path \"%s\".", getForkPath()));
		if (!ret.isDirectory())
			throw new IOException(String.format(
				"Target \"%s\" is not a directory.", getForkPath()));
		
		return ret;
	}
	
	public WorkingDirectoryFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType add(EndpointReferenceType exemplarEPR,
			String entryName, EndpointReferenceType entry) throws IOException
	{
		throw new IOException(
			"Not allowed to add arbitrary endpoints to a " +
			"bes-activity working directory.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR,
			String newFileName) throws IOException
	{
		File targetDir = getTargetDirectory();
		File newFile = new File(targetDir, newFileName);
		if (newFile.createNewFile())
		{
			String forkPath = formForkPath(newFileName);
			ResourceForkService service = getService();
			
			return service.createForkEPR(forkPath,
				new WorkingDirFileFork(service, forkPath).describe());
		}
		
		throw new IOException("Unable to create new file.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR,
			String entryName) throws IOException
	{
		File targetDir = getTargetDirectory();
		
		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
		
		ResourceForkInformation info;
		for (File entry : targetDir.listFiles())
		{
			if (entryName == null || entry.getName().equals(entryName))
			{
				if (entry.isDirectory())
					info = new WorkingDirectoryFork(getService(),
						formForkPath(entry.getName())).describe();
				else
					info = new WorkingDirFileFork(getService(),
						formForkPath(entry.getName())).describe();
				
				entries.add(createInternalEntry(
					exemplarEPR, entry.getName(), info));
			}
		}
			
		return entries;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR,
			String newDirectoryName) throws IOException
	{
		File targetDir = getTargetDirectory();
		File newFile = new File(targetDir, newDirectoryName);
		if (newFile.mkdir())
		{
			String forkPath = formForkPath(newDirectoryName);
			ResourceForkService service = getService();
			
			return service.createForkEPR(forkPath,
				new WorkingDirectoryFork(service, forkPath).describe());
		}
		
		throw new IOException("Unable to create new directory.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public boolean remove(String entryName) throws IOException
	{
		File targetDir = getTargetDirectory();
		File newFile = new File(targetDir, entryName);
		if (newFile.delete())
			return true;

		return false;
	}
}