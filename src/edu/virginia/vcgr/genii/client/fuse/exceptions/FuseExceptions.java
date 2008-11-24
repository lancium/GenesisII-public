package edu.virginia.vcgr.genii.client.fuse.exceptions;

import edu.virginia.vcgr.fsii.exceptions.FSBadFileHandleException;
import edu.virginia.vcgr.fsii.exceptions.FSEntryAlreadyExistsException;
import edu.virginia.vcgr.fsii.exceptions.FSEntryNotFoundException;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.exceptions.FSFileHandleBadStateException;
import edu.virginia.vcgr.fsii.exceptions.FSIllegalAccessException;
import edu.virginia.vcgr.fsii.exceptions.FSInvalidFileHandleException;
import edu.virginia.vcgr.fsii.exceptions.FSNotADirectoryException;
import edu.virginia.vcgr.fsii.exceptions.FSNotAFileException;
import edu.virginia.vcgr.fsii.exceptions.FSRuntimeException;
import edu.virginia.vcgr.fsii.exceptions.FSSecurityException;
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
		else if (cause instanceof FSBadFileHandleException)
			return new FuseBadFileHandleException(message, cause);
		else if (cause instanceof FSEntryAlreadyExistsException)
			return new FuseEntryAlreadyExistsException(message, cause);
		else if (cause instanceof FSEntryNotFoundException)
			return new FuseNoSuchEntryException(message, cause);
		else if (cause instanceof FSException)
			return new FuseException(message, cause);
		else if (cause instanceof FSFileHandleBadStateException)
			return new FuseFileHandleBadStateException(message, cause);
		else if (cause instanceof FSIllegalAccessException)
			return new FuseIllegalAccessException(message, cause);
		else if (cause instanceof FSInvalidFileHandleException)
			return new FuseBadFileHandleException(message, cause);
		else if (cause instanceof FSNotADirectoryException)
			return new FuseEntryNotDirectoryException(message, cause);
		else if (cause instanceof FSNotAFileException)
			return new FuseEntryIsDirectoryException(message, cause);
		else if (cause instanceof FSRuntimeException)
			return translate(message, cause.getCause());
		else if (cause instanceof FSSecurityException)
			return new FusePermissionDeniedException(message, cause);
		else				
			return new FuseUnknownException(message, cause);
	}
}