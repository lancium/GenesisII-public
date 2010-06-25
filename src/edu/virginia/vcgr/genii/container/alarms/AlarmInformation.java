package edu.virginia.vcgr.genii.container.alarms;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;

class AlarmInformation
{
	private long _alarmID;
	private long _repeatInterval;
	private ICallingContext _callingContext;
	private EndpointReferenceType _target;
	private String _methodName;
	private Object _userData;

	AlarmInformation(long alarmID, long repeatInterval,
		ICallingContext callingContext, EndpointReferenceType target,
		String methodName, Object userData)
	{
		_alarmID = alarmID;
		_repeatInterval = repeatInterval;
		_callingContext = callingContext;
		_target = target;
		_methodName = methodName;
		_userData = userData;
	}
	
	final long alarmID()
	{
		return _alarmID;
	}
	
	final long repeatInterval()
	{
		return _repeatInterval;
	}
	
	final ICallingContext callingContext()
	{
		return _callingContext;
	}
	
	final EndpointReferenceType target()
	{
		return _target;
	}
	
	final String methodName()
	{
		return _methodName;
	}
	
	final Object userData()
	{
		return _userData;
	}
}