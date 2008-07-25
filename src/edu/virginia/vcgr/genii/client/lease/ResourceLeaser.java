package edu.virginia.vcgr.genii.client.lease;

import java.io.Closeable;

import org.morgan.util.io.StreamUtils;

public abstract class ResourceLeaser<ResourceType>
{
	private int _resourceLimit;
	private LRUList<LeaseableResourceImpl> _outstandingLeases =
		new LRUList<LeaseableResourceImpl>();
	
	protected abstract ResourceType createNewResource();
	
	public ResourceLeaser(int resourceLimit)
	{
		_resourceLimit = resourceLimit;
	}
	
	public LeaseableResource<ResourceType> obtainLease(
		LeaseeAgreement<ResourceType> agreement)
	{
		synchronized(_outstandingLeases)
		{
			if (_outstandingLeases.size() < _resourceLimit)
			{
				LeaseableResourceImpl lease = 
					new LeaseableResourceImpl(createNewResource(), agreement);
				_outstandingLeases.add(lease);
				return lease;
			} else
			{
				LeaseableResourceImpl lease = _outstandingLeases.pop();
				lease._agreement.relinquish(lease);
				lease._agreement = agreement;
				_outstandingLeases.add(lease);
				return lease;
			}
		}
	}
	
	private class LeaseableResourceImpl extends LRUList.LRUNode 
		implements LeaseableResource<ResourceType>, Closeable
	{
		private LeaseeAgreement<ResourceType> _agreement;
		private ResourceType _resource;
		
		private LeaseableResourceImpl(ResourceType resource,
			LeaseeAgreement<ResourceType> agreement)
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
			if (_resource != null)
			{
				synchronized(_outstandingLeases)
				{
					_outstandingLeases.remove(this);
				}
				
				if (_resource instanceof Closeable)
					StreamUtils.close((Closeable)_resource);
				
				_resource = null;
			}
		}

		@Override
		public ResourceType resource()
		{
			synchronized(_outstandingLeases)
			{
				_outstandingLeases.noteUse(this);
			}
			
			return _resource;
		}	
	}
}