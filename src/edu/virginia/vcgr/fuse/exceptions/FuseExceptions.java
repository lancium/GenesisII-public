package edu.virginia.vcgr.fuse.exceptions;

import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import fuse.FuseException;

public class FuseExceptions
{
	static public FuseException translate(String message, Throwable cause)
	{
		if (cause instanceof FuseException)
			return (FuseException)cause;
		else if (cause instanceof RNSPathAlreadyExistsException)
			return new FuseEntryAlreadyExistsException(message, cause);
		else if (cause instanceof RNSPathDoesNotExistException)
			return new FuseNoSuchEntryException(message, cause);
		else if (cause instanceof GenesisIISecurityException)
			return new FusePermissionDeniedException(message, cause);
		else if (cause instanceof RNSException)
			return new FuseEntryNotDirectoryException(message, cause);
		else
			return new FuseUnknownException(message, cause);
	}
}