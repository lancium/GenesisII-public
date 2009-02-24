package edu.virginia.vcgr.genii.client.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.axis.types.UnsignedInt;
import org.ggf.jsdl.JobDefinition_Type;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.iterator.WSIterable;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.queue.ConfigureRequestType;
import edu.virginia.vcgr.genii.queue.JobInformationType;
import edu.virginia.vcgr.genii.queue.JobStateEnumerationType;
import edu.virginia.vcgr.genii.queue.QueuePortType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;
import edu.virginia.vcgr.genii.queue.SubmitJobRequestType;

public class QueueManipulator
{
	private EndpointReferenceType _queue;

	public QueueManipulator(String queuePath)
		throws RNSException
	{
		RNSPath path = RNSPath.getCurrent().lookup(queuePath, RNSPathQueryFlags.MUST_EXIST);
		_queue = path.getEndpoint();
	}
	
	public QueueManipulator(EndpointReferenceType queue)
	{
		_queue = queue;
	}
	
	public JobTicket submit(File jsdlFile, int priority)
		throws FileNotFoundException, ResourceException,
			RemoteException
	{
		FileInputStream fin = null;
		try
		{
			fin = new FileInputStream(jsdlFile);
			return submit(fin, priority);
		} finally
		{
			StreamUtils.close(fin);
		}
	}
	
	public JobTicket submit(InputStream in, int priority)
		throws ResourceException, RemoteException
	{
		return submit(
			(JobDefinition_Type)ObjectDeserializer.deserialize(
				new InputSource(in), JobDefinition_Type.class),
			priority);
	}
	
	public JobTicket submit(JobDefinition_Type jobDef, int priority)
		throws RemoteException
	{
		QueuePortType queue = ClientUtils.createProxy(QueuePortType.class, _queue);
		return new JobTicket(queue.submitJob(new SubmitJobRequestType(
			jobDef, (byte)priority)).getJobTicket());
	}
	
	public Iterator<JobInformation> status(Collection<JobTicket> jobs)
		throws RemoteException
	{
		QueuePortType queue = ClientUtils.createProxy(QueuePortType.class, _queue);
		String []jobTickets = null;
		
		if (jobs != null)
		{
			jobTickets = new String[jobs.size()];
			int lcv = 0;
			for (JobTicket ticket : jobs)
				jobTickets[lcv++] = ticket.toString();
		}
		
		WSIterable<JobInformationType> iterable = null;
		try
		{
			iterable = new WSIterable<JobInformationType>(
				JobInformationType.class, 
				queue.iterateStatus(jobTickets).getResult(), 200, true);
			return new JobInformationIterator(iterable.iterator());
		}
		finally
		{
			StreamUtils.close(iterable);
		}
	}
	
	public Iterator<ReducedJobInformation> list()
		throws RemoteException
	{
		QueuePortType queue = ClientUtils.createProxy(QueuePortType.class, _queue);
		
		WSIterable<ReducedJobInformationType> iterable = null;
		try
		{
			iterable = new WSIterable<ReducedJobInformationType>(
				ReducedJobInformationType.class, 
				queue.iterateListJobs(null).getResult(), 200, true);
			return new ReducedJobInformationIterator(iterable.iterator());
		}
		finally
		{
			StreamUtils.close(iterable);
		}
	}
	
	public void kill(Collection<JobTicket> jobs)
		throws RemoteException
	{
		String []tickets = new String[jobs.size()];
		int lcv = 0;
		for (JobTicket ticket : jobs)
		{
			tickets[lcv++] = ticket.toString();
		}
		
		QueuePortType queue = ClientUtils.createProxy(QueuePortType.class, _queue);
		queue.killJobs(tickets);
	}
	
	public void complete(Collection<JobTicket> jobs)
		throws RemoteException
	{
		String []tickets = new String[jobs.size()];
		int lcv = 0;
		for (JobTicket ticket : jobs)
		{
			tickets[lcv++] = ticket.toString();
		}
		
		QueuePortType queue = ClientUtils.createProxy(QueuePortType.class, _queue);
		queue.completeJobs(tickets);
	}
	
	public void completeAll()
		throws RemoteException
	{
		QueuePortType queue = ClientUtils.createProxy(QueuePortType.class, _queue);
		queue.completeJobs(null);
	}
	
	public void configure(String resourceName, int numSlots)
		throws RemoteException
	{
		QueuePortType queue = ClientUtils.createProxy(QueuePortType.class, _queue);
		queue.configureResource(new ConfigureRequestType(resourceName,
			new UnsignedInt((long)numSlots)));
	}
	
	static private class ReducedJobInformationIterator
		implements Iterator<ReducedJobInformation>
	{
		private Iterator<ReducedJobInformationType> _jit;
		
		public ReducedJobInformationIterator(Iterator<ReducedJobInformationType> jit)
		{
			_jit = jit;
		}
		
		@Override
		public boolean hasNext()
		{
			return _jit.hasNext();
		}

		@Override
		public ReducedJobInformation next()
		{
			ReducedJobInformationType jobInfo = _jit.next();
			JobTicket ticket = new JobTicket(jobInfo.getJobTicket());
			JobStateEnumerationType state = jobInfo.getJobStatus();
			byte [][]ownerBytes = jobInfo.getOwner();
			Collection<Identity> identities;
			try
			{
				identities = QueueUtils.deserializeIdentities(ownerBytes);
			}
			catch (Exception e)
			{
				throw new RuntimeException("Unable to deserialize owner identities.", e);
			}
			
			return new ReducedJobInformation(ticket, 
					identities, 
					QueueStates.fromQueueStateType(state));		
		}

		@Override
		public void remove()
		{
			_jit.remove();
		}
	}
	
	static private class JobInformationIterator 
		implements Iterator<JobInformation>
	{
		private Iterator<JobInformationType> _jit;
		
		public JobInformationIterator(Iterator<JobInformationType> jit)
		{
			_jit = jit;
		}
		
		@Override
		public boolean hasNext()
		{
			return _jit.hasNext();
		}

		@Override
		public JobInformation next()
		{
			JobInformationType jobInfo = _jit.next();
			JobTicket ticket = new JobTicket(jobInfo.getJobTicket());
			byte [][]owners = jobInfo.getOwner();
			Collection<Identity> identities;
			try
			{
				identities = QueueUtils.deserializeIdentities(owners);
			}
			catch (Exception e)
			{
				throw new RuntimeException("Unable to deserialize owner identities.", e);
			}
			
			return new JobInformation(ticket, identities,
					QueueStates.fromQueueStateType(jobInfo.getJobStatus()),
					(int)jobInfo.getPriority(), jobInfo.getSubmitTime(),
					jobInfo.getStartTime(), jobInfo.getFinishTime(),
					jobInfo.getAttempts().intValue(),
					jobInfo.getScheduledOn());	
		}

		@Override
		public void remove()
		{
			_jit.remove();
		}
	}
}