package edu.virginia.vcgr.genii.container.cservices.infomgr;

class BlockingInformationListener<InformationType>
	implements InformationListener<InformationType>
{
	private Object _lock = new Object();
	private InformationResult<InformationType> _result = null;
	
	InformationResult<InformationType> get()
		throws InterruptedException
	{
		synchronized(_lock)
		{
			while (_result == null)
				_lock.wait();
			
			return _result;
		}
	}
	
	@Override
	public void informationUpdated(InformationEndpoint endpoint,
			InformationResult<InformationType> information)
	{
		synchronized(_lock)
		{
			_result = information;
			_lock.notifyAll();
		}
	}
}