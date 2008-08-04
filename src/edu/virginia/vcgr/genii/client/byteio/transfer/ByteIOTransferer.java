package edu.virginia.vcgr.genii.client.byteio.transfer;

import org.apache.axis.types.URI;

/**
 * A transferer is the generic interface for any class that
 * can transfer data to or from a remote ByteIO.  The protocol
 * for ByteIO transfer is extensible making it possible for a
 * virtually unlimited number of transfer options.
 * 
 * @author mmm2a
 */
public interface ByteIOTransferer
{
	/**
	 * Retrieve the transfer protocol supported by this transferer.
	 * 
	 * @return A URI that identifies the transfer protocol supported
	 * by this transferer instance.
	 */
	public URI getTransferProtocol();
	
	public int getMaximumReadSize();
	
	public int getMaximumWriteSize();
	
	/**
	 * Different transfer mechanisms have different optimal transfer
	 * sizes.  While that optimal number depends on some factors that
	 * can't be predicted (bandwidth, latency, network congestion, etc.),
	 * a transferer can generally give a good estimate of what reasonable
	 * transfer size is.  This method returns the preferred block size
	 * for reading from a remote ByteIO using this transfer mechanism.
	 * 
	 * @return The preferred block size (in bytes) to use when transferring
	 * data using this transferer.
	 */
	public int getPreferredReadSize();
	
	/**
	 * Different transfer mechanisms have different optimal transfer
	 * sizes.  While that optimal number depends on some factors that
	 * can't be predicted (bandwidth, latency, network congestion, etc.),
	 * a transferer can generally give a good estimate of what reasonable
	 * transfer size is.  This method returns the preferred block size
	 * for writing to a remote ByteIO using this transfer mechanism.
	 * 
	 * @return The preferred block size (in bytes) to use when transferring
	 * data using this transferer.
	 */
	public int getPreferredWriteSize();
}