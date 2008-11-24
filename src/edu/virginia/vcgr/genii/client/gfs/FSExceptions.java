package edu.virginia.vcgr.genii.client.gfs;

import edu.virginia.vcgr.fsii.exceptions.FSEntryAlreadyExistsException;
import edu.virginia.vcgr.fsii.exceptions.FSEntryNotFoundException;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.exceptions.FSNotADirectoryException;
import edu.virginia.vcgr.fsii.exceptions.FSSecurityException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

public class FSExceptions
{
	static public FSException translate(String message, Throwable cause)
	{
		if (cause instanceof FSException)
			return (FSException)cause;
		else if (cause instanceof RNSPathAlreadyExistsException)
			return new FSEntryAlreadyExistsException(message, cause);
		else if (cause instanceof RNSPathDoesNotExistException)
			return new FSEntryNotFoundException(message, cause);
		else if (cause instanceof GenesisIISecurityException)
			return new FSSecurityException(message, cause);
		else if (cause instanceof RNSException)
			return new FSNotADirectoryException(message, cause);
		else
			return new FSException(message, cause);
	}
}