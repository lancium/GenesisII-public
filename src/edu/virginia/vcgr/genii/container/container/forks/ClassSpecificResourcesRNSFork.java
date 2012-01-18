package edu.virginia.vcgr.genii.container.container.forks;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.resource.db.query.ResourceSummary;
import edu.virginia.vcgr.genii.container.resource.db.query.ResourceSummaryInformation;
import edu.virginia.vcgr.genii.container.rfork.ReadOnlyRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.security.RWXCategory;

public class ClassSpecificResourcesRNSFork extends ReadOnlyRNSResourceFork
{
	static private Log _logger = LogFactory.getLog(ClassSpecificResourcesRNSFork.class);
	
	static private Collection<ResourceSummaryInformation> getSummaryInfo(
		Connection connection, String forkPath) throws SQLException
	{
		Map<String, Collection<ResourceSummaryInformation>> map =
			ResourceSummary.resources(connection);
		for (String originalName : map.keySet())
		{
			String name = originalName;
			int index = name.lastIndexOf('.');
			if (index >= 0)
				name = name.substring(index + 1);
			
			if (forkPath.endsWith("/" + name))
				return map.get(originalName);
		}
		
		return new Vector<ResourceSummaryInformation>();
	}
	
	static private String formEntryName(ResourceSummaryInformation info)
	{
		String human = info.humanName();
		if (human != null)
		{
			int index = human.lastIndexOf('/');
			if (index >= 0)
				human = human.substring(index + 1);
			return human;
		}
		
		return info.epi();
	}
	
	public ClassSpecificResourcesRNSFork(
		ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	@RWXMapping(RWXCategory.CLOSED)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR,
			String entryName) throws IOException
	{
		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
		IResource resource = getService().getResourceKey().dereference();
		Connection connection = ((BasicDBResource)resource).getConnection();

		try
		{
			Collection<ResourceSummaryInformation> summaryInfo =
				getSummaryInfo(connection, getForkPath());
			
			for (ResourceSummaryInformation entry : summaryInfo)
			{
				String name = formEntryName(entry);
				
				if (entryName == null || entryName.equals(name))
				{
					entries.add(new InternalEntry(
						name, ResourceSummary.getEPR(connection,
							entry.resourceID())));
				}
			}
		}
		catch (SQLException sqe)
		{
			_logger.warn("Unable to get list of resources.", sqe);
		}
		
		return entries;
	}
}