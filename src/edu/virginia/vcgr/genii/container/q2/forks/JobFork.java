package edu.virginia.vcgr.genii.container.q2.forks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.AbstractRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.security.RWXCategory;

final public class JobFork extends AbstractRNSResourceFork
{
	static private Log _logger = LogFactory.getLog(JobFork.class);
	
	public JobFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	final public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR,
		String entryName) throws IOException
	{
		Collection<InternalEntry> entries = new ArrayList<InternalEntry>(2);
		String jobTicket = getForkName();
		
		if (entryName == null || entryName.equals("status"))
			entries.add(createInternalEntry(
				exemplarEPR, "status",
					new JobInformationFork(getService(),
						formForkPath("status")).describe()));
		
		if (entryName == null || entryName.equals("activity"))
		{
			try
			{
				ResourceKey rKey = getService().getResourceKey();
				QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());
				EndpointReferenceType epr = mgr.getActivityEPR(jobTicket);
				if (epr != null)
					entries.add(new InternalEntry("activity", epr, null));
			}
			catch (Throwable cause)
			{
				_logger.warn(
					"Error trying to get activity information for fork!", 
					cause);
			}
		}
		
		return entries;
	}

	@Override
	@RWXMapping(RWXCategory.CLOSED)
	final public EndpointReferenceType add(EndpointReferenceType exemplarEPR,
		String entryName, EndpointReferenceType entry) throws IOException
	{
		throw new UnsupportedOperationException(
			"Not permitted to add new entries to a Queue job fork!");
	}

	@Override
	@RWXMapping(RWXCategory.CLOSED)
	final public boolean remove(String entryName) throws IOException
	{
		throw new UnsupportedOperationException(
			"Not permitted to remove entries from a Queue job fork!");
	}

	@Override
	@RWXMapping(RWXCategory.CLOSED)
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR,
		String newFileName) throws IOException
	{
		throw new UnsupportedOperationException(
			"Not permitted to add new entries to a Queue job fork!");
	}

	@Override
	@RWXMapping(RWXCategory.CLOSED)
	public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR,
			String newDirectoryName) throws IOException
	{
		throw new UnsupportedOperationException(
		"Not permitted to add new entries to a Queue job fork!");
	}
}
