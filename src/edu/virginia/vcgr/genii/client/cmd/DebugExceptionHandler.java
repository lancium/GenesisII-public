package edu.virginia.vcgr.genii.client.cmd;

import java.io.PrintStream;

public class DebugExceptionHandler implements IExceptionHandler
{
	public int handleException(Throwable cause, PrintStream errorStream)
	{
		errorStream.println(cause.getLocalizedMessage());
		cause.printStackTrace(errorStream);
		
		return 1;
	}
}