package edu.virginia.vcgr.genii.container.invoker;

import java.util.HashMap;
import java.util.Map;

public class ServiceInitializationLocker implements IAroundInvoker
{
	static private class ServiceState
	{
		volatile private boolean _initialized = false;

		synchronized public void waitUntilInitialized()
		{
			while (!_initialized) {
				try {
					wait();
				} catch (InterruptedException ie) {
				}
			}
		}

		synchronized public void setInitialized()
		{
			_initialized = true;
			notifyAll();
		}
	}

	static private Map<Class<?>, ServiceState> _serviceStates = new HashMap<Class<?>, ServiceState>();

	static private ServiceState getServiceState(Class<?> serviceClass)
	{
		synchronized (_serviceStates) {
			ServiceState ret = _serviceStates.get(serviceClass);
			if (ret == null)
				_serviceStates.put(serviceClass, ret = new ServiceState());

			return ret;
		}
	}

	@Override
	public Object invoke(InvocationContext invocationContext) throws Exception
	{
		ServiceState state = getServiceState(invocationContext.getTarget().getClass());
		state.waitUntilInitialized();
		return invocationContext.proceed();
	}

	static public void setInitialized(Class<?> serviceClass)
	{
		ServiceState state = getServiceState(serviceClass);
		state.setInitialized();
	}
}