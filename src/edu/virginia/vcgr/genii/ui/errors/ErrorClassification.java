package edu.virginia.vcgr.genii.ui.errors;

import java.io.InterruptedIOException;

import org.apache.axis.AxisFault;

import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

public enum ErrorClassification
{
	IGNORABLE(),
	ROUTINE(),
	UNEXPECTED();
	
	static public ClassifiedError classify(Throwable cause)
	{
		if (cause instanceof InterruptedException)
			return new ClassifiedError(IGNORABLE, null, null, cause);
		else if (cause instanceof InterruptedIOException)
			return new ClassifiedError(IGNORABLE, null, null, cause);
		else if (cause instanceof GenesisIISecurityException)
			return new ClassifiedError(ROUTINE, "Security Exception",
				"Permission denied", cause);
		else if (cause instanceof AxisFault)
			return new ClassifiedError(ROUTINE, "Grid Communication Fault",
				cause.toString(), cause);
		
		return new ClassifiedError(UNEXPECTED, null, null, cause);
	}
}