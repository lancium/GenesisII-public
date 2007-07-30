package edu.virginia.vcgr.genii.container.resource;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

/**
 * Resource factories are solely responsible for instantiating the java class
 * for a given resource.  It is up to the ResourceKey class to call load or
 * initialize.
 * 
 * @author Mark Morgan (mmm2a@cs.virginia.edu)
 */
public interface IResourceFactory
{
	/**
	 * Instiante a new resource class.
	 * 
	 * @return The new resource class.
	 */
	public IResource instantiate(ResourceKey parentKey) throws ResourceException;
}