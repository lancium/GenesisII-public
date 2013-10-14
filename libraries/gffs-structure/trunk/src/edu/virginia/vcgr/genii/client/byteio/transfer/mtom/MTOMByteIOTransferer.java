package edu.virginia.vcgr.genii.client.byteio.transfer.mtom;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;

/**
 * An interface for all MTOM transferers. This interface is basically a convenient place to put some
 * constants relevant to the MTOM transfer protocol.
 * 
 * @author mmm2a
 */
public interface MTOMByteIOTransferer
{
	static public final URI TRANSFER_PROTOCOL = ByteIOConstants.TRANSFER_TYPE_MTOM_URI;

	static public final int PREFERRED_READ_SIZE = 1024 * 1024 * 8 * ByteIOConstants.numThreads;
	static public final int MAXIMUM_READ_SIZE = -1;
	static public final int PREFERRED_WRITE_SIZE = PREFERRED_READ_SIZE;
	static public final int MAXIMUM_WRITE_SIZE = MAXIMUM_READ_SIZE;
}