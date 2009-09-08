package edu.virginia.vcgr.genii.ui.errors;

import java.io.InterruptedIOException;

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
		
		return new ClassifiedError(UNEXPECTED, null, null, cause);
	}
}