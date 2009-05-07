package edu.virginia.vcgr.genii.container.cservices.infomgr;

import java.util.Calendar;

public class WaitingListener<InformationType>
{
	private Calendar _timeout;
	private InformationListener<InformationType> _listener;
	
	WaitingListener(InformationListener<InformationType> listener,
		Calendar timeout)
	{
		_timeout = timeout;
		_listener = listener;
	}
	
	final Calendar getTimeout()
	{
		return _timeout;
	}
	
	final InformationListener<InformationType> getListener()
	{
		return _listener;
	}
}