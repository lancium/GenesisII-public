package edu.virginia.vcgr.genii.client.cmd;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.oasis_open.wsrf.basefaults.BaseFaultType;

public class SimpleExceptionHandler implements IExceptionHandler
{
	public int handleException(Throwable cause, PrintStream errorStream)
	{
		if (cause instanceof NullPointerException)
			errorStream.println("Internal Genesis II Error -- Null Pointer Exception");
		else if (cause instanceof FileNotFoundException)
			errorStream.println("File Not Found:  " + cause.getLocalizedMessage());
		else if (cause instanceof BaseFaultType)
			errorStream.println(((BaseFaultType)cause).getDescription(0).get_value());
		else 
			errorStream.println(cause.getLocalizedMessage());
		
		return 1;
	}
}