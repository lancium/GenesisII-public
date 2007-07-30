package edu.virginia.vcgr.genii.client.cmd;

import java.io.PrintStream;

public class SimpleExceptionHandler implements IExceptionHandler
{
	public int handleException(Throwable cause, PrintStream errorStream)
	{
		errorStream.println(cause.getLocalizedMessage());
		
		return 1;
	}
}