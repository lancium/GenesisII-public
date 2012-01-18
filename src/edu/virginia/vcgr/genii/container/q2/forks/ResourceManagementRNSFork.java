package edu.virginia.vcgr.genii.container.q2.forks;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.ReadOnlyRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.rns.LegacyEntryType;
import edu.virginia.vcgr.genii.security.RWXCategory;

public class ResourceManagementRNSFork extends ReadOnlyRNSResourceFork
{
	public ResourceManagementRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
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
				ret.add(createInternalEntry(
					exemplarEPR, entry.getEntry_name(),
					new ResourceManagementCmdFork(getService(),
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