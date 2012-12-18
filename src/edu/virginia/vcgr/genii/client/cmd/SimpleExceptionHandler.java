package edu.virginia.vcgr.genii.client.cmd;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.axis.AxisFault;
import org.oasis_open.wsrf.basefaults.BaseFaultType;

import edu.virginia.vcgr.genii.client.security.authz.PermissionDeniedException;
import edu.virginia.vcgr.genii.client.version.MinimumVersionException;

public class SimpleExceptionHandler implements IExceptionHandler
{
	public int handleException(Throwable cause, Writer eStream)
	{
		PrintWriter errorStream;
		
		if (eStream instanceof PrintWriter)
			errorStream = (PrintWriter)eStream;
		else
			errorStream = new PrintWriter(eStream);
		
		String tab = "";
		StringBuilder builder = new StringBuilder();

		while (cause != null)
		{
			if (cause instanceof MinimumVersionException)
			{
				MinimumVersionException me = (MinimumVersionException)cause;
				builder.append(tab + String.format(
					"Your client at version \"%s\" doesn't appear to meet\n" +
					"the minimum version requirements for the target\n" +
					"operation (%s).  Please exit the grid shell and\n" +
					"run the grid-update tool to upgrade your client.", 
					me.getClientVersion(), me.getMinimumVersion()));
				errorStream.print(builder);
				errorStream.flush();
				return 1;
			} else if (cause instanceof java.lang.OutOfMemoryError) {
				builder.append(tab + "The client has run out of memory.  This could be fixed by increasing\n"
						+ "the maximum memory allowed for the JVM.  On Linux, try changing -Xmx512M\n"
						+ "to -Xmx1G or more.  On Windows, pass the memory to the grid launcher with a\n"
						+ "-J flag first, e.g. grid.exe -J-Xmx1G");
				errorStream.print(builder);
				errorStream.flush();
				// re-throw to cause the grid client to exit.
				throw new java.lang.OutOfMemoryError();
			} else if (cause instanceof NullPointerException) {
				builder.append(tab + 
					"Internal Genesis II Error -- Null Pointer Exception\n");
			} else if (cause instanceof FileNotFoundException) {
				builder.append(tab + "File Not Found:  " +
					cause.getLocalizedMessage() + "\n");
			} else if (cause instanceof BaseFaultType) {
				builder.append(tab + 
					((BaseFaultType)cause).getDescription(0).get_value() +
					"\n");
			} else if (cause instanceof AxisFault) {
				String message = cause.getLocalizedMessage();
				
				/* Check to see if it's a permission denied exception */
				String operation = null;
				String failedAsset = null;
				operation = PermissionDeniedException.extractMethodName(cause.getMessage());
				failedAsset = PermissionDeniedException.extractAssetDenied(cause.getMessage());
				if ( (operation != null) && (failedAsset != null) ) {
					builder.append(tab + "Permission denied on \"" + failedAsset + "\" (in method \"" + 
						operation + "\").\n");
				} else {
					MinimumVersionException mve =
						MinimumVersionException.reformException(message);
					if (mve != null)
					{
						handleException(mve, eStream);
						return 1;
					} else
					{
						builder.append(tab + message + "\n");
					}
				}
			} else
				builder.append(tab + cause.getLocalizedMessage() + "\n");

			tab = tab + "    ";
			cause = cause.getCause();
		}

		errorStream.print(builder);
		errorStream.flush();
		
		return 1;
	}
}
