package edu.virginia.vcgr.genii.container.queue.resource;

import java.util.Collection;
import java.util.regex.Pattern;

import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.rns.EntryType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.queue.JobInformationType;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;

/**
 * This is the Queue's resource type.  As long as the queue resource implements this
 * interface, it should work with the queue.
 * 
 * @author mmm2a
 */
public interface IQueueResource extends IResource
{
	// Resources
	public void addResource(String resourceName, 
		EndpointReferenceType resourceEndpoint) throws ResourceException;
	public Collection<EntryType> listResources(Pattern pattern) 
		throws ResourceException;
	public Collection<String> remove(Pattern pattern)
		throws ResourceException;
	public void configureResource(String resourceName, int numSlots)
		throws ResourceException;
	
	// Jobs
	public void submitJob(ICallingContext callingContext,
		String jobTicket, JobDefinition_Type jsdl, int priority,
		Collection<Identity> owners) throws ResourceException;
	public JobInformationType[] getStatus(String []jobTicket)
		throws ResourceException;
	public ReducedJobInformationType[] listJobs() throws ResourceException;
	public void killJobs(String []tickets) throws ResourceException;
	public void complete(String []tickets) throws ResourceException;
	public void completeAll() throws ResourceException;
}