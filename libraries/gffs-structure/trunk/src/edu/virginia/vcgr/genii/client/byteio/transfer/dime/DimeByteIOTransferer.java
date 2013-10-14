package edu.virginia.vcgr.genii.client.byteio.transfer.dime;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;

/**
 * An interface for all DIME transferers. This interface is basically a convenient place to put some
 * constants relevant to the DIME transfer protocol.
 * 
 * @author mmm2a
 */
public interface DimeByteIOTransferer
{
	static final public URI TRANSFER_PROTOCOL = ByteIOConstants.TRANSFER_TYPE_DIME_URI;

	static final public int PREFERRED_READ_SIZE = 1024 * 1024 * 1 * ByteIOConstants.numThreads;
	static final public int MAXIMUM_READ_SIZE = PREFERRED_READ_SIZE;
	static final public int PREFERRED_WRITE_SIZE = PREFERRED_READ_SIZE;
	static final public int MAXIMUM_WRITE_SIZE = MAXIMUM_READ_SIZE;
}
