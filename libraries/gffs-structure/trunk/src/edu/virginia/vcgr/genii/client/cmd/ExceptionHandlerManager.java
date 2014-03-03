package edu.virginia.vcgr.genii.client.cmd;

import edu.virginia.vcgr.genii.client.configuration.UserPreferences;

public class ExceptionHandlerManager
{
	synchronized static public IExceptionHandler getExceptionHandler()
	{
		return UserPreferences.preferences().getExceptionHandler();
	}
}
