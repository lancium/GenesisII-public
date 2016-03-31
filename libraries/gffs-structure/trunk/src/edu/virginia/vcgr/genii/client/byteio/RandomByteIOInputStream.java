package edu.virginia.vcgr.genii.client.byteio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.parallelByteIO.FastRead;
import edu.virginia.vcgr.genii.client.byteio.parallelByteIO.FillerAndChecker;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.byteio.transfer.dime.DimeByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.mtom.MTOMByteIOTransferer;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;

/**
 * An implementation of the standard Java Input stream that reads from remote Random ByteIO resources.
 * 
 * @author mmm2a
 */
public class RandomByteIOInputStream extends InputStream
{
	static Log _logger = LogFactory.getLog(RandomByteIOInputStream.class);

	/* The current offset within the remote random byteio resource; only makes sense for local tracking! */
	private long _offset = 0L;

	/* The following denotes the list of variables added for multiThreaded byteIO */
	private boolean isMultiThreaded = true; // denoting if we are doing parallel byteIO
	private int numThreads = 1; // number of parallel threads, calculated in constructor.
	private RandomByteIOTransferer[] transferer; // Each transferer denotes a unique end-point
	private long _fileSize = 0; // record the full file size at time of construction.

	private int _protocolReadBlockSize = 0; // filled in based on type of resource in constructor.

	/**
	 * Create a new RandomByteIO input stream for a given endpoint and transfer protocol.
	 * 
	 * @param source
	 *            The source ByteIO to read bytes from.
	 * @param desiredTransferProtocol
	 *            The desired transfer protocol to use when reading bytes.
	 */
	public RandomByteIOInputStream(EndpointReferenceType source, URI desiredTransferProtocol) throws RemoteException, IOException
	{
		TypeInformation typeInfo = new TypeInformation(source);
		// ASG: First figure out the max read size for the protocol, mtom, whatever
		// If there is no type info let's increase by 8
		_protocolReadBlockSize = ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE * 8;
		if (desiredTransferProtocol != null) {
			if (desiredTransferProtocol.equals(ByteIOConstants.TRANSFER_TYPE_MTOM)) {
				// keep in mind that preferred read size has a *numThreads
				_protocolReadBlockSize = MTOMByteIOTransferer.PREFERRED_READ_SIZE;
			}
			if (desiredTransferProtocol.equals(ByteIOConstants.TRANSFER_TYPE_DIME)) {
				// keep in mind that preferred read size has a *numThreads
				_protocolReadBlockSize = DimeByteIOTransferer.PREFERRED_READ_SIZE;
			}
		}

		// we can't just spawn endless threads, or we'll choke on the memory being used. so we use a constant.
		numThreads = ByteIOConstants.NUMBER_OF_THREADS_FOR_BYTEIO_PARALLEL_READS;

		// ASG: Then figure out how big the file is.
		_fileSize = typeInfo.getByteIOSize();

		// ASG: Then if the file is smaller than a single read, don't bother doing anything in parallel.
		if ((numThreads <= 1) || (_fileSize < _protocolReadBlockSize)) {
			isMultiThreaded = false;
			numThreads = 1;
		}

//		if (!isMultiThreaded) {
//			RandomByteIOPortType clientStub = ClientUtils.createProxy(RandomByteIOPortType.class, source);
//			RandomByteIOTransfererFactory factory = new RandomByteIOTransfererFactory(clientStub);
//			transferer = new RandomByteIOTransferer[1];
//			transferer[0] = factory.createRandomByteIOTransferer(desiredTransferProtocol);
//		} else {
//			// The number of threads is >=2 (i.e) we will have parallelism

			RandomByteIOPortType[] clientStub = new RandomByteIOPortType[numThreads];
			RandomByteIOTransfererFactory[] factory = new RandomByteIOTransfererFactory[numThreads];
			transferer = new RandomByteIOTransferer[numThreads];

			for (int lcv = 0; lcv < numThreads; lcv++) {
				clientStub[lcv] = ClientUtils.createProxy(RandomByteIOPortType.class, source);
				factory[lcv] = new RandomByteIOTransfererFactory(clientStub[lcv]);
				transferer[lcv] = factory[lcv].createRandomByteIOTransferer(desiredTransferProtocol);
			}
//		}
	}

	/**
	 * Create a new RandomByteIO input stream for a given endpoint.
	 * 
	 * @param source
	 *            The source ByteIO to read bytes from.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public RandomByteIOInputStream(EndpointReferenceType source) throws IOException, RemoteException
	{
		this(source, null);
	}

	/**
	 * A convenience method for reading a certain number of bytes from the target ByteIO.
	 * 
	 * @param length
	 *            The number of bytes to read.
	 * 
	 * @return The block of data (if any) just read.
	 * 
	 * @throws IOException
	 */
	private byte[] read(int length) throws IOException
	{
		if (length + _offset > _fileSize) {
			// this check assumes we are not overly concerned about reading a file that is still growing.
			// but if we do try to read past the end, all sorts of bad things happen.
			length = (int) (_fileSize - _offset);
			if (length <= 0) {
				// nothing to read, we believe.
				return new byte[0];
			}
			_logger.debug("shortening read length to " + length + " due to file being too short.");
		}
		
		if (!isMultiThreaded || (numThreads <= 1) || (length <= _protocolReadBlockSize)) {
			byte[] data = transferer[0].read(_offset, length, 1, 0);
			_offset += data.length;
			return data;
		} else {
			/*
			 * denotes block-size which each thread reads. the last thread may not read this exact amount. we bias the calculation by the
			 * number of threads to ensure we never read too little due to round off errors.
			 */
			int perThreadReadBlockSize = ((length + numThreads) / numThreads);

			Thread[] threads = new Thread[numThreads];
			FastRead[] readers = new FastRead[numThreads];

			CountDownLatch latch = new CountDownLatch(numThreads);
			FillerAndChecker readAssembler = new FillerAndChecker(latch, length);

			int thisThreadsOffset = 0;

			long remainingToRead = length;
			int whichThread = 0;
			while (remainingToRead > 0) {
				int currentBlockSize = perThreadReadBlockSize;
				if (remainingToRead <= perThreadReadBlockSize) {
					currentBlockSize = (int) remainingToRead;
				}

				readers[whichThread] =
					new FastRead(transferer[whichThread], _offset + thisThreadsOffset, currentBlockSize, readAssembler, whichThread, perThreadReadBlockSize);
				threads[whichThread] = new Thread(readers[whichThread]);
				remainingToRead -= currentBlockSize;
				thisThreadsOffset += currentBlockSize;
				whichThread++;

				// we don't want to blow any of our constraints.
				if (remainingToRead < 0) {
					String msg = "logic error in RandomByteIOInputStream parallel reads: remaining left to read underflowed";
					throw new IOException(msg);
				} else if ((remainingToRead > 0) && (whichThread >= numThreads)) {
					String msg = "logic error in RandomByteIOInputStream parallel reads: thread counter overflowed";
					throw new IOException(msg);
				}
			}
			if (whichThread != numThreads) {
				String msg = "logic error in RandomByteIOInputStream parallel reads: did not use all threads for parallel read";
				throw new IOException(msg);
			} else if (remainingToRead != 0) {
				String msg = "logic error in RandomByteIOInputStream parallel reads: there was remaining data to read that no thread is handling";
				throw new IOException(msg);
			}

			for (int i = 0; i < numThreads; i++) {
				threads[i].start();
			}

			try {
				readAssembler.await();
			} catch (InterruptedException ie) {
				throw new IOException(ie);
			}

			if (readAssembler.isErrorFlag())
				throw new IOException(readAssembler.getThreadFailCause());

			int lastFilledBufferIndex = readAssembler.getLastFilledBufferIndex();
			if (lastFilledBufferIndex != -1) {
				// I have fetched some data
				//hmmm: clean logging levels.
				_logger.debug("rbyteio read: i fetched some data");
				if (lastFilledBufferIndex != (length - 1)) {
					// I have fetched only a subset of the requested amount!
					_logger.debug("rbyteio read: the read only fetched a subset of what was requested; index is " + lastFilledBufferIndex);
					byte[] temp_data = new byte[lastFilledBufferIndex + 1];
					System.arraycopy(readAssembler.getData(), 0, temp_data, 0, lastFilledBufferIndex + 1);
					_offset += temp_data.length;
					return temp_data;
				} else {
					// I have fetched the requested amount !
					_logger.debug("rbyteio read: the read fetched the entire length requested, last index at " + lastFilledBufferIndex);
					_offset += readAssembler.getData().length;
					return readAssembler.getData();
				}
			} else {
				// Attempt to read 0 bytes.
				return new byte[0];
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException
	{
		byte[] data = read(1);
		if (data.length == 1)
			return data[0];

		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b) throws IOException
	{
		byte[] data = read(b.length);
		System.arraycopy(data, 0, b, 0, data.length);
		return (data.length == 0) ? -1 : data.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		byte[] data = read(len);
		System.arraycopy(data, 0, b, off, data.length);
		return (data.length == 0) ? -1 : data.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long skip(long n)
	{
		_offset += n;
		return _offset;
	}

	/**
	 * A convenience method for creating a buffered stream (using the current transferer's preferred buffering size) from this input stream.
	 * 
	 * @return The newly created buffered input stream.
	 */
	public BufferedInputStream createPreferredBufferedStream()
	{
		return new BufferedInputStream(this, transferer[0].getPreferredReadSize());
	}

}