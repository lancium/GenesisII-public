package edu.virginia.vcgr.genii.client.byteio.transfer.dime;

import org.apache.axis.types.URI;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;

public interface DimeByteIOTransferer
{
	static final public URI TRANSFER_PROTOCOL = 
		ByteIOConstants.TRANSFER_TYPE_DIME_URI;
	
	static final public int PREFERRED_READ_SIZE = 1024*1024;
	static final public int PREFERRED_WRITE_SIZE = PREFERRED_READ_SIZE;
}
