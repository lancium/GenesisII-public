package edu.virginia.vcgr.genii.container.q2.forks;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.AbstractRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.rns.LegacyEntryType;
import edu.virginia.vcgr.genii.security.RWXCategory;

public class ResourcesRNSFork extends AbstractRNSResourceFork
{
	public ResourcesRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType add(EndpointReferenceType exemplarEPR,
			String entryName, EndpointReferenceType entry) throws IOException
	{
		ResourceKey rKey = getService().getResourceKey();
		
		try
		{
			QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());
			mgr.addNewBES(entryName, entry);
			return entry;
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unable to add bes container.", sqe);
		}
	}

	@Override
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR,
			String newFileName) throws IOException
	{
		throw new IOException(
			"This RNS directory only permits BES containers to be linked in.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR,
			String entryName) throws IOException
	{
		ResourceKey rKey = getService().getResourceKey();
		Collection<LegacyEntryType> entries;
		Collection<InternalEntry> ret;
		
		try
		{
			QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());
			entries = mgr.listBESs(entryName);
			ret = new ArrayList<InternalEntry>(entries.size());
			for (LegacyEntryType entry : entries)
			{
				ret.add(new InternalEntry(entry.getEntry_name(), 
					entry.getEntry_reference(), entry.get_any()));
			}
			
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unable to add bes container.", sqe);
		}
	}

	@Override
	public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR,
			String newDirectoryName) throws IOException
	{
		throw new IOException(
			"This RNS directory only permits BES containers to be linked in.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public boolean remove(String entryName) throws IOException
	{
		ResourceKey rKey = getService().getResourceKey();
		Collection<String> entries = new ArrayList<String>();
		
		try
		{
			QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());
			entries = mgr.removeBESs(entryName);
			
			return entries.size() > 0;
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unable to add bes container.", sqe);
		}
	}
}