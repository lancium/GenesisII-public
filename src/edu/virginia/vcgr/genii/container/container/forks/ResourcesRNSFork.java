package edu.virginia.vcgr.genii.container.container.forks;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.resource.db.query.ResourceSummary;
import edu.virginia.vcgr.genii.container.resource.db.query.ResourceSummaryInformation;
import edu.virginia.vcgr.genii.container.rfork.ReadOnlyRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

public class ResourcesRNSFork extends ReadOnlyRNSResourceFork
{
	static private Log _logger = LogFactory.getLog(ResourcesRNSFork.class);

	public ResourcesRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.CLOSED)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR, String entryName) throws IOException
	{
		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
		ResourceForkInformation info;
		IResource resource = getService().getResourceKey().dereference();
		Connection connection = ((BasicDBResource) resource).getConnection();

		if (entryName == null || entryName.equals("summary")) {
			info = new ResourcesSummaryFork(getService(), formForkPath("summary")).describe();
			entries.add(createInternalEntry(exemplarEPR, "summary", info));
		}

		try {
			Map<String, Collection<ResourceSummaryInformation>> map = ResourceSummary.resources(connection);
			for (String key : map.keySet()) {
				int index = key.lastIndexOf('.');
				if (index >= 0)
					key = key.substring(index + 1);

				if (entryName == null || entryName.equals(key)) {
					info = new ClassSpecificResourcesRNSFork(getService(), formForkPath(key)).describe();
					entries.add(createInternalEntry(exemplarEPR, key, info));
				}
			}
		} catch (SQLException sqe) {
			_logger.warn("Unable to get list of resources.", sqe);
		}

		return entries;
	}
}