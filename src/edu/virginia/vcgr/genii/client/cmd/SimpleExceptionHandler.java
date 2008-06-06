package edu.virginia.vcgr.genii.client.cmd;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.axis.AxisFault;
import org.oasis_open.wsrf.basefaults.BaseFaultType;

import edu.virginia.vcgr.genii.client.security.gamlauthz.PermissionDeniedException;

public class SimpleExceptionHandler implements IExceptionHandler
{
	public int handleException(Throwable cause, PrintStream errorStream)
	{
		String tab = "";
		StringBuilder builder = new StringBuilder();

		while (cause != null)
		{
			if (cause instanceof NullPointerException)
				builder.append(tab + 
					"Internal Genesis II Error -- Null Pointer Exception\n");
			else if (cause instanceof FileNotFoundException)
				builder.append(tab + "File Not Found:  " +
					cause.getLocalizedMessage() + "\n");
			else if (cause instanceof BaseFaultType)
				builder.append(tab + 
					((BaseFaultType)cause).getDescription(0).get_value() +
					"\n");
			else if (cause instanceof AxisFault)
			{
				String message = cause.getLocalizedMessage();
				
				/* Check to see if it's a permission denied exception */
				String operation = PermissionDeniedException.extractMethodName(message);
				if (operation != null)
				{
					builder.append(tab + "Permission denied for method \"" + 
						operation + "\".\n");
				} else
				{
					builder.append(tab + message + "\n");
				}
			} else
				builder.append(tab + cause.getLocalizedMessage() + "\n");

			tab = tab + "    ";
			cause = cause.getCause();
		}

		errorStream.print(builder);
		return 1;
	}
}
