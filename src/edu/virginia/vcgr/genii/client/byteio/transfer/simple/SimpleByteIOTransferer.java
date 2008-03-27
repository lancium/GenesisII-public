package edu.virginia.vcgr.genii.client.byteio.transfer.simple;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;

public interface SimpleByteIOTransferer
{
	static final public URI TRANSFER_PROTOCOL = ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI;
	
	static final public int PREFERRED_READ_SIZE = 1024*256*2;
	static final public int PREFERRED_WRITE_SIZE = PREFERRED_READ_SIZE;
}