package edu.virginia.vcgr.genii.client.lease;

public interface LeaseeAgreement<Type>
{
	public LeaseableResource<Type> relinquish(
		LeaseableResource<Type> lease);
}