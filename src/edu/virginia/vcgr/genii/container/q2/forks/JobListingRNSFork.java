package edu.virginia.vcgr.genii.container.q2.forks;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.AbstractRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.queue.JobStateEnumerationType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;

public class JobListingRNSFork extends AbstractRNSResourceFork
{
	public JobListingRNSFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType add(EndpointReferenceType exemplarEPR,
			String entryName, EndpointReferenceType entry) throws IOException
	{
		throw new IOException(
			"Cannot add new jobs using this RNS directory.");
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR,
			String newFileName) throws IOException
	{
		throw new IOException(
			"Cannot create new files in this directory.");
	}
	
	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR,
			String entryName) throws IOException
	{
		boolean mineOnly = false;
		String forkPath = getForkPath();
		String []acceptableStatuses = null;

		if (forkPath.endsWith("/all"))
			acceptableStatuses = new String[] { 
				JobStateEnumerationType._ERROR,
				JobStateEnumerationType._FINISHED,
				JobStateEnumerationType._QUEUED,
				JobStateEnumerationType._REQUEUED,
				JobStateEnumerationType._RUNNING,
				JobStateEnumerationType._STARTING
			};
		else if (forkPath.endsWith("/queued"))
			acceptableStatuses = new String[] { 
				JobStateEnumerationType._QUEUED,
				JobStateEnumerationType._REQUEUED
			};
		else if (forkPath.endsWith("/running"))
			acceptableStatuses = new String[] { 
				JobStateEnumerationType._RUNNING,
				JobStateEnumerationType._STARTING
			};
		else if (forkPath.endsWith("/finished"))
			acceptableStatuses = new String[] {
				JobStateEnumerationType._ERROR,
				JobStateEnumerationType._FINISHED
			};
		
		if (forkPath.startsWith("/jobs/mine/"))
			mineOnly = true;
			
		ResourceKey rKey = getService().getResourceKey();
		Collection<? extends ReducedJobInformationType> jobs;
		Collection<InternalEntry> ret = new LinkedList<InternalEntry>();
		
		try
		{
			QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());
			if (!mineOnly)
				jobs = mgr.listJobs(null);
			else
				jobs = mgr.getJobStatus(null);
			
			for (ReducedJobInformationType job : jobs)
			{
				if (entryName != null && !job.getJobTicket().equals(entryName))
					continue;
				
				boolean passes = false;
				JobStateEnumerationType status = job.getJobStatus();
				for (String acc : acceptableStatuses)
				{
					if (acc.equals(status.getValue()))
					{
						passes = true;
						break;
					}
				}
				
				if (!passes)
					continue;
				
				ret.add(createInternalEntry(exemplarEPR, job.getJobTicket(),
					new JobInformationFork(getService(), 
						formForkPath(job.getJobTicket())).describe()));
			}
			
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to list jobs in queue.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR,
			String newDirectoryName) throws IOException
	{
		throw new IOException(
			"Not allowed to make new directories inside of this directory.");
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public boolean remove(String entryName) throws IOException
	{
		String forkPath = getForkPath();
		
		if (!forkPath.startsWith("/jobs/mine"))
			throw new IOException(
				"Jobs can only be removed from the \"mine\" fork.");
			
		String ticket = entryName;
		ResourceKey rKey = getService().getResourceKey();
		
		try
		{
			QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());
			mgr.killJobs(new String[] { ticket });
			
			for (int attempt = 0; attempt < 5; attempt++)
			{
				try
				{
					mgr.completeJobs(new String[] { ticket });
					return true;
				}
				catch (ResourceException re)
				{
					if (re.getLocalizedMessage().contains(
						"is not in a final state"))
					{
						try { Thread.sleep(1000L << attempt); } 
						catch (InterruptedException ie) {}
					} else
						throw re;
				}
			}
			
			throw new IOException("Unable to complete job after kill.");
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to remove job from queue.", sqe);
		}
	}
}