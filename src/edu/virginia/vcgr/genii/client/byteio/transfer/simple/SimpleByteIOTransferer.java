package edu.virginia.vcgr.genii.client.byteio.transfer.simple;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;

/**
 * An interface for all Simple transferers. This interface is basically a convenient place to put
 * some constants relevant to the Simple transfer protocol.
 * 
 * @author mmm2a
 */
public interface SimpleByteIOTransferer
{
	static final public URI TRANSFER_PROTOCOL = ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI;

	static final public int PREFERRED_READ_SIZE = 1024 * 1024 * 3 * ByteIOConstants.numThreads;
	static final public int MAXIMUM_READ_SIZE = -1;
	static final public int PREFERRED_WRITE_SIZE = PREFERRED_READ_SIZE;
	static final public int MAXIMUM_WRITE_SIZE = MAXIMUM_READ_SIZE;
}