package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.container.rfork.AbstractRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.RNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;

public class LightWeightExportDirFork extends AbstractRNSResourceFork
	implements RNSResourceFork
{
	final private VExportDir getTarget() throws IOException
	{
		return LightWeightExportUtils.getDirectory(getForkPath());
	}
	
	public LightWeightExportDirFork(ResourceForkService service,
			String forkPath)
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
			"light-weight export.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR,
			String newFileName) throws IOException
	{
		VExportDir dir = getTarget();
		if (dir.createFile(newFileName))
		{
			String forkPath = formForkPath(newFileName);
			ResourceForkService service = getService();
			
			return service.createForkEPR(forkPath,
				new LightWeightExportFileFork(service, forkPath).describe());
		}
		
		throw new IOException("Unable to create new file.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR,
			String entryName) throws IOException
	{
		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
		VExportDir dir = getTarget();
		
		for (VExportEntry dirEntry : dir.list(entryName))
		{
			String dName = dirEntry.getName();
			
			if (entryName == null || entryName.equals(dName))
			{
				ResourceForkInformation info;
				
				if (dirEntry.isDirectory())
					info = new LightWeightExportDirFork(getService(),
						formForkPath(dName)).describe();
				else
					info = new LightWeightExportFileFork(getService(),
						formForkPath(dName)).describe();
				
				entries.add(createInternalEntry(exemplarEPR, dName, info));
			}
		}
		
		return entries;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR,
			String newDirectoryName) throws IOException
	{
		VExportDir dir = getTarget();
		if (dir.mkdir(newDirectoryName))
		{
			String forkPath = formForkPath(newDirectoryName);
			ResourceForkService service = getService();
			
			return service.createForkEPR(forkPath,
				new LightWeightExportDirFork(service, forkPath).describe());
		}
		
		throw new IOException("Unable to create new directory.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public boolean remove(String entryName) throws IOException
	{
		VExportDir dir = getTarget();
		return dir.remove(entryName);
	}
}