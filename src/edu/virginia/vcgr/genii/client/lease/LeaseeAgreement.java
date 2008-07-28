package edu.virginia.vcgr.genii.client.lease;

/**
 * A lease agreement is an agreement made by a leasee which allows the leaser
 * to reclaim a resource that was leased out.  All leasee's MUST support this
 * interface and are required to pass an instance of this interface to the 
 * leaser to use at his/her own discretion.  No guarantees are made by the
 * leaser about the length of leases and all leasees must be prepared to
 * relinquish a resource at any time.
 * 
 * @author mmm2a
 *
 * @param <Type>
 */
public interface LeaseeAgreement<Type>
{
	/**
	 * Signifies a request made by a leaser for a leasee to relinquish a
	 * resource.
	 * 
	 * @param lease The lease that is being relinquished.
	 * 
	 * @return The resource that was relinquished.
	 */
	public LeaseableResource<Type> relinquish(
		LeaseableResource<Type> lease);
}