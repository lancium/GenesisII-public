package edu.virginia.vcgr.genii.client.cmd;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class SimpleExceptionHandler implements IExceptionHandler
{
	public int handleException(Throwable cause, PrintStream errorStream)
	{
		if (cause instanceof NullPointerException)
			errorStream.println("Internal Genesis II Error -- Null Pointer Exception");
		else if (cause instanceof FileNotFoundException)
			errorStream.println("File Not Found:  " + cause.getLocalizedMessage());
		else
			errorStream.println(cause.getLocalizedMessage());
		
		return 1;
	}
}