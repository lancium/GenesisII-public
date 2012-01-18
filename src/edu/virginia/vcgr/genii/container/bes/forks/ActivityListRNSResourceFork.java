package edu.virginia.vcgr.genii.container.bes.forks;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.TerminateActivityResponseType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.bes.GeniiBESServiceImpl;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.bes.activity.NoSuchActivityFault;
import edu.virginia.vcgr.genii.container.bes.resource.IBESResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.AbstractRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.security.RWXCategory;

public class ActivityListRNSResourceFork extends AbstractRNSResourceFork
{
	static private Log _logger = LogFactory.getLog(
		ActivityListRNSResourceFork.class);
	
	public ActivityListRNSResourceFork(ResourceForkService service,
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
			"Not allowed to add new activites to this resource fork.");
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR,
			String newFileName) throws IOException
	{
		throw new IOException(
			"Not allowed to add new activites to this resource fork.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR,
			String entryName) throws IOException
	{
		Collection<InternalEntry> response =
			new LinkedList<InternalEntry>();
		
		IBESResource resource = 
			(IBESResource)ResourceManager.getCurrentResource().dereference();
		
		try
		{
			String query = entryName;
			for (BESActivity activity : resource.getContainedActivities())
			{
				String name = activity.getJobName();
				if (query == null || query.equals(name))
				{
					try
					{
						response.add(new InternalEntry(
							name, activity.getActivityEPR()));
					}
					catch (NoSuchActivityFault nsaf)
					{
						_logger.debug("We lost an activity between the " +
							"time we looked it up and the time we got " +
							"it's EPR.", nsaf);
					}
				}
			}
			
			return response;
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unexpected BES exception.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR,
			String newDirectoryName) throws IOException
	{
		throw new IOException(
			"Not allowed to make new directory using this resource fork.");
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public boolean remove(String entryName) throws IOException
	{
		boolean failed = false;
		IBESResource resource = 
			(IBESResource)ResourceManager.getCurrentResource().dereference();
		
		try
		{
			String query = entryName;
			for (BESActivity activity : resource.getContainedActivities())
			{
				String name = activity.getJobName();
				if (query == null || query.equals(name))
				{
					TerminateActivityResponseType tat;
					try
					{
						tat = GeniiBESServiceImpl.terminateActivity(
							activity.getActivityEPR());
						if (tat.getFault() != null)
						{
							_logger.error("Unable to remove activity \"" + 
								name + "\":  " + tat.getFault());
							failed = true;
						}
					}
					catch (NoSuchActivityFault nsaf)
					{
						_logger.debug("We lost an activity between the time " +
							"we looked it up and when we asked for it's EPR.", 
							nsaf);
					}
				}
			}
			
			return !failed;
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unexpected BES exception.", sqe);
		}
	}
}
