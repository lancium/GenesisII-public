package edu.virginia.vcgr.genii.container.cservices.infomgr;

import java.util.Calendar;

public class InformationResult<InfoType>
{
	private Throwable _exception;
	private Calendar _lastUpdated;
	private InfoType _information;
	
	public InformationResult(InfoType information, Calendar lastUpdated,
		Throwable exception)
	{
		_exception = exception;
		_lastUpdated = lastUpdated;
		_information = information;
	}
	
	public InformationResult(InfoType information, Calendar lastUpdated)
	{
		this(information, lastUpdated, null);
	}
	
	final public boolean wasResponsive()
	{
		return _exception == null;
	}
	
	final public Calendar lastUpdated()
	{
		return _lastUpdated;
	}
	
	final public InfoType information()
	{
		return _information;
	}
	
	final public Throwable exception()
	{
		return _exception;
	}
}