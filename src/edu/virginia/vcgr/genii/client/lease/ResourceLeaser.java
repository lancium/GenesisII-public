package edu.virginia.vcgr.genii.client.lease;

import java.io.Closeable;

import org.morgan.util.io.StreamUtils;

/**
 * This class represents the base level functionallity of an entity which can lease out resources
 * for use.
 * 
 * @author mmm2a
 * 
 * @param <ResourceType>
 */
public abstract class ResourceLeaser<ResourceType>
{
	private int _resourceLimit;
	private LRUList<LeaseableResourceImpl> _outstandingLeases = new LRUList<LeaseableResourceImpl>();

	/**
	 * An abstract method that derived classes will need to override that can create a new resource
	 * to lease out.
	 * 
	 * @return The newly created resource.
	 */
	protected abstract ResourceType createNewResource();

	/**
	 * Construct a new leaser with some number of resources available.
	 * 
	 * @param resourceLimit
	 *            The number of resources to make available.
	 */
	public ResourceLeaser(int resourceLimit)
	{
		_resourceLimit = resourceLimit;
	}

	/**
	 * A method that leasees can call to obtain a new lease on some resource.
	 * 
	 * @param agreement
	 *            The agreement that the leasee is making with the leaser (which will allow the
	 *            leaser to reclaim the resource later if need be).
	 * 
	 * @return The newly obtained resource.
	 */
	public LeaseableResource<ResourceType> obtainLease(LeaseeAgreement<ResourceType> agreement)
	{
		LeaseableResourceImpl lease;
		LeaseableResource<ResourceType> tmp;

		while (true) {
			synchronized (_outstandingLeases) {
				if (_outstandingLeases.size() < _resourceLimit) {
					lease = new LeaseableResourceImpl(createNewResource(), agreement);
					_outstandingLeases.add(lease);
					return lease;
				}

				lease = _outstandingLeases.pop();
			}

			tmp = lease._agreement.relinquish(lease);

			synchronized (_outstandingLeases) {
				if (tmp == null)
					continue;

				lease._agreement = agreement;
				_outstandingLeases.add(lease);
				return lease;
			}
		}
	}

	/**
	 * Internal implementationof a leaseable resource element.
	 * 
	 * @author mmm2a
	 */
	private class LeaseableResourceImpl extends LRUList.LRUNode implements LeaseableResource<ResourceType>, Closeable
	{
		private LeaseeAgreement<ResourceType> _agreement;
		private ResourceType _resource;

		private LeaseableResourceImpl(ResourceType resource, LeaseeAgreement<ResourceType> agreement)
		{
			_resource = resource;
			_agreement = agreement;
		}

		@Override
		protected void finalize()
		{
			close();
		}

		@Override
		public void cancel()
		{
			close();
		}

		@Override
		synchronized public void close()
		{
			if (_resource != null) {
				synchronized (_outstandingLeases) {
					_outstandingLeases.remove(this);
				}

				if (_resource instanceof Closeable)
					StreamUtils.close((Closeable) _resource);

				_resource = null;
			}
		}

		@Override
		public ResourceType resource()
		{
			synchronized (_outstandingLeases) {
				_outstandingLeases.noteUse(this);
			}

			return _resource;
		}
	}
}