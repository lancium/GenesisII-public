package edu.virginia.vcgr.genii.container.rfork.persprop;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.AbstractRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;

public class PersistedPropertyRNSFork extends AbstractRNSResourceFork
{
	public PersistedPropertyRNSFork(ResourceForkService service, 
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
			"Not allowed to link arbitrary resources " +
			"into persisted property resource forks.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR,
		String newFileName) throws IOException
	{
		String category = getForkName();
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		resource.replacePersistedProperty(category,
			newFileName, "Placeholder");
		
		String forkPath = formForkPath(newFileName);
		ResourceForkService service = getService();
		
		return service.createForkEPR(forkPath,
			new PersistedPropertyByteIO(service, forkPath).describe());
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR,
			String entryName) throws IOException
	{
		Collection<InternalEntry> ret = new LinkedList<InternalEntry>();
		
		String category = getForkName();
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		Properties props = resource.getPersistedProperties(category);
		
		for (Object keyObj : props.keySet())
		{
			String key = keyObj.toString();
			if (entryName == null || entryName.equals(key))
			{
				ret.add(createInternalEntry(exemplarEPR, key,
					new PersistedPropertyByteIO(getService(), 
						formForkPath(key)).describe()));
			}
		}
		
		return ret;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR,
			String newDirectoryName) throws IOException
	{
		throw new IOException("Not allowed to make new directories " +
			"inside of persisted property resource forks.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public boolean remove(String entryName) throws IOException
	{
		String category = getForkName();
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		resource.removePersistedProperty(category, entryName);
		return true;
	}
}