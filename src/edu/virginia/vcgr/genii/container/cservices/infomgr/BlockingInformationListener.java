package edu.virginia.vcgr.genii.container.cservices.infomgr;

/**
 * The Blocking Information Listener is a special listener, used internally in this package, that
 * allows the caller to block until it's information listener interface is called. We use this in
 * the InformationPortal when a users asked to making a blocking call for information.
 * 
 * @author mmm2a
 * 
 * @param <InformationType>
 */
class BlockingInformationListener<InformationType> implements InformationListener<InformationType>
{
	private Object _lock = new Object();
	private InformationResult<InformationType> _result = null;

	/**
	 * Get (and block until available) the information that this listener was listening for.
	 * 
	 * @return The information result object that was set by the listener interface.
	 * @throws InterruptedException
	 *             If the thread is interrupted while waiting.
	 */
	InformationResult<InformationType> get() throws InterruptedException
	{
		synchronized (_lock) {
			/*
			 * Until the result is non-null, we haven't been called yet.
			 */
			while (_result == null)
				_lock.wait();

			return _result;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void informationUpdated(InformationEndpoint endpoint, InformationResult<InformationType> information)
	{
		synchronized (_lock) {
			_result = information;
			_lock.notifyAll();
		}
	}
}