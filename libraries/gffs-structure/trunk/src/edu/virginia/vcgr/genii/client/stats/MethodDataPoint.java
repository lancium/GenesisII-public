package edu.virginia.vcgr.genii.client.stats;

import java.lang.reflect.Method;

public class MethodDataPoint implements DataPoint
{
	private Class<?> _serviceClass;
	private Method _serviceMethod;
	private long _startTime;
	private long _completeTime;
	private boolean _successfull;

	public MethodDataPoint(Class<?> serviceClass, Method serviceMethod)
	{
		_serviceClass = serviceClass;
		_serviceMethod = serviceMethod;
		_startTime = System.currentTimeMillis();
		_completeTime = -1L;
		_successfull = false;
	}

	public void complete(boolean successfull)
	{
		_completeTime = System.currentTimeMillis();
		_successfull = successfull;
	}

	public Class<?> serviceClass()
	{
		return _serviceClass;
	}

	public Method serviceMethod()
	{
		return _serviceMethod;
	}

	public boolean isCompleted()
	{
		return _completeTime >= 0L;
	}

	public long duration()
	{
		return _completeTime - _startTime;
	}

	public boolean successfull()
	{
		return _successfull;
	}

	public boolean withinWindow(long currentTime, long windowSize)
	{
		return _startTime >= (currentTime - windowSize);
	}
}