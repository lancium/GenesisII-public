package edu.virginia.vcgr.genii.container.q2.forks;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.ggf.rns.EntryType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.ReadOnlyRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;

public class ResourceSlotsRNSFork extends ReadOnlyRNSResourceFork
{
	public ResourceSlotsRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR,
			String entryName) throws IOException
	{
		ResourceKey rKey = getService().getResourceKey();
		Collection<EntryType> entries;
		Collection<InternalEntry> ret;
		
		try
		{
			QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());
			entries = mgr.listBESs(entryName);
			ret = new ArrayList<InternalEntry>(entries.size());
			for (EntryType entry : entries)
			{
				ret.add(createInternalEntry(
					exemplarEPR, entry.getEntry_name(),
					new ResourceSlotStateFork(getService(),
						formForkPath(entry.getEntry_name())).describe()));
			}
			
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unable to list resource slots.", sqe);
		}
	}
}