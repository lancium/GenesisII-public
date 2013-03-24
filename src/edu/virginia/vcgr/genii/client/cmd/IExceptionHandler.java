package edu.virginia.vcgr.genii.client.cmd;

import java.io.Writer;

public interface IExceptionHandler
{
	public int handleException(Throwable cause, Writer errorStream);
}