package edu.virginia.vcgr.genii.client.cmd;

import java.io.PrintWriter;
import java.io.Writer;

public class DebugExceptionHandler implements IExceptionHandler
{
	public int handleException(Throwable cause, Writer errorStream)
	{
		PrintWriter pw = null;
		if (errorStream instanceof PrintWriter)
			pw = (PrintWriter) errorStream;
		else
			pw = new PrintWriter(errorStream);

		pw.println(cause.getLocalizedMessage());
		cause.printStackTrace(pw);
		pw.flush();

		return 1;
	}
}