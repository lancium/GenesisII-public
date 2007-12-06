package edu.virginia.vcgr.ogrsh.server.exceptions;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHReadBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHWriteBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IPackable;

public class OGRSHException extends Exception implements IPackable
{
	static private Log _logger = LogFactory.getLog(OGRSHException.class);
	
	static final long serialVersionUID = 0L;
	
	static public final int EXCEPTION_UNKNOWN = 0;
	static public final int EXCEPTION_UNKNOWN_FUNCTION = 1;
	static public final int EXCEPTION_CORRUPTED_REQUEST = 2;
	static public final int EXCEPTION_DIVIDE_BY_ZERO = 3;
	static public final int MALFORMED_URL = 4;
	static public final int IO_EXCEPTION = 5;
	static public final int UNKNOWN_SESSION_EXCEPTION = 6;
	static public final int PATH_DOES_NOT_EXIST = 7;
	static public final int PATH_ALREADY_EXISTS = 8;
	static public final int PERMISSION_DENIED = 9;
	static public final int NOT_A_DIRECTORY = 10;
	static public final int DIRECTORY_NOT_EMPTY = 11;
	static public final int EACCES = PERMISSION_DENIED;
	static public final int EEXIST = PATH_ALREADY_EXISTS;
	static public final int ENOENT = PATH_DOES_NOT_EXIST;
	static public final int ENOTDIR = NOT_A_DIRECTORY;
	static public final int EBADF = 12;
	static public final int EISDIR = 13;
	static public final int EROFS = 14;
	
	private int _exceptionNumber;
	
	public OGRSHException(String msg, Throwable cause)
	{
		super((msg == null) ? cause.getLocalizedMessage() : msg, cause);
		
		if (cause instanceof OGRSHException)
			_exceptionNumber = ((OGRSHException)cause)._exceptionNumber;
		else if (cause instanceof RNSPathAlreadyExistsException)
			_exceptionNumber = OGRSHException.PATH_ALREADY_EXISTS;
		else if (cause instanceof RNSPathDoesNotExistException)
			_exceptionNumber = OGRSHException.PATH_DOES_NOT_EXIST;
		else if (cause instanceof GenesisIISecurityException)
			_exceptionNumber = OGRSHException.PERMISSION_DENIED;
		else if (cause instanceof RNSException)
			_exceptionNumber = OGRSHException.NOT_A_DIRECTORY;
		else
			_exceptionNumber = OGRSHException.EXCEPTION_UNKNOWN;
		
		_logger.debug("OGRSHException(" 
			+ getLocalizedMessage() + ", " + _exceptionNumber 
			+ ") thrown because of another exception.", cause);
	}
	
	public OGRSHException(Throwable cause)
	{
		this(cause.getLocalizedMessage(), cause);
	}
	
	public OGRSHException(int exceptionNumber, String message)
	{
		this(message, exceptionNumber);
	}
	
	public OGRSHException(String message, int exceptionNumber)
	{
		super(message);
		
		_exceptionNumber = exceptionNumber;
	}
	
	public OGRSHException(IOGRSHReadBuffer readBuffer) throws IOException
	{
		this(Integer.class.cast(readBuffer.readObject()), 
			String.class.cast(readBuffer.readObject()));
	}
	
	public int getExceptionNumber()
	{
		return _exceptionNumber;
	}
	
	public void pack(IOGRSHWriteBuffer writeBuffer) throws IOException
	{
		writeBuffer.writeObject(_exceptionNumber);
		writeBuffer.writeObject(getLocalizedMessage());
	}
	
	public void unpack(IOGRSHReadBuffer readBuffer) throws IOException
	{
		_exceptionNumber = Integer.class.cast(readBuffer.readObject());
		readBuffer.readObject();
	}
}
