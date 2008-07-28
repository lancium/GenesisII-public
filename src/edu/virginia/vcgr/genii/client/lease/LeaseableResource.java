package edu.virginia.vcgr.genii.client.lease;

/**
 * This interface represents a resource that can be leased out by a leaser,
 * and relinquished by a leasee.
 * 
 * @author mmm2a
 *
 * @param <ResourceType>
 */
public interface LeaseableResource<ResourceType>
{
	/**
	 * Cancel the lease.  This method can be called by a leasee to signify that
	 * the resource is no longer needed.  Once this method is called, the
	 * leaser can no longer make requests to the leasee to relinqish the 
	 * associated resource (as that lease is no longer valid).
	 */
	public void cancel();
	
	/**
	 * Retrieve the actual resource covered by this lease.
	 * @return The resource covered by this lease.
	 */
	public ResourceType resource();
}