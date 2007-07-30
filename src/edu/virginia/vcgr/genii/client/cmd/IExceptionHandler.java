package edu.virginia.vcgr.genii.client.cmd;

import java.io.PrintStream;

public interface IExceptionHandler
{
	public int handleException(Throwable cause,
		PrintStream errorStream);
}