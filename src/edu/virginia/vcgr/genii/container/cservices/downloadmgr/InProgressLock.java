package edu.virginia.vcgr.genii.container.cservices.downloadmgr;

import java.io.IOException;

public class InProgressLock
{
	private IOException _exception = null;
	
	public void setException(IOException cause)
	{
		_exception = cause;
	}
	
	public void checkException() throws IOException
	{
		if (_exception != null)
			throw _exception;
	}
}