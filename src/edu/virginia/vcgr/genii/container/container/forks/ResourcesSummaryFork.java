package edu.virginia.vcgr.genii.container.container.forks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.resource.db.query.ResourceSummary;
import edu.virginia.vcgr.genii.container.resource.db.query.ResourceSummaryInformation;
import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

public class ResourcesSummaryFork extends
		AbstractStreamableByteIOFactoryResourceFork
{
	public ResourcesSummaryFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void modifyState(InputStream source) throws IOException
	{
		throw new IOException("Not allowed to modify the the resources summary.");
	}
	
	@Override
	@RWXMapping(RWXCategory.READ)
	public void snapshotState(OutputStream sink) throws IOException
	{
		PrintStream ps = new PrintStream(sink);
		long total = 0;
		
		try
		{
			ResourceKey key = getService().getResourceKey();
			IResource resource = key.dereference();
			if (resource instanceof BasicDBResource)
			{
				Connection connection = 
					((BasicDBResource)resource).getConnection();
				Map<String, Collection<ResourceSummaryInformation>> map =
					ResourceSummary.resources(connection);
				
				List<String> keys = new Vector<String>(map.keySet());
				Collections.sort(keys);
				for (String className : keys)
				{
					Collection<ResourceSummaryInformation> summaryInfo =
						map.get(className);
					if (summaryInfo != null)
					{
						total += summaryInfo.size();
						
						ps.format("Number of resource for \"%s\":  %d\n", className, 
							summaryInfo.size());
					}
				}
				
				ps.println();
				ps.format("Total number of resources contained:  %d\n", total);
			}
		}
		catch (Throwable cause)
		{
			ps.println("Unable to get resources summary.");
			cause.printStackTrace(ps);
		}
		
		ps.flush();
		ps.close();
	}
}