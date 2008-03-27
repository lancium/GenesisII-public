package edu.virginia.vcgr.genii.client.byteio.transfer;

import org.apache.axis.types.URI;

public interface ByteIOTransferer
{
	public URI getTransferProtocol();
	
	public int getPreferredReadSize();
	public int getPreferredWriteSize();
}