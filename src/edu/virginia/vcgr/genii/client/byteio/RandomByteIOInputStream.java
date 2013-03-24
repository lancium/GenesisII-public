package edu.virginia.vcgr.genii.client.byteio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;

import org.apache.axis.types.URI;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.parallelByteIO.FastRead;
import edu.virginia.vcgr.genii.client.byteio.parallelByteIO.FillerAndChecker;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;

/**
 * An implementation of the standard Java Input stream that reads from remote Random ByteIO
 * resources.
 * 
 * @author mmm2a
 */
public class RandomByteIOInputStream extends InputStream
{

	/* The current offset within the remote random byteio resource */
	private long _offset = 0L;

	/* The following denotes the list of variables added for multiThreaded byteIO */
	private boolean isMultiThreaded = true; // denoting if we are doing parallel byteIO
	private int numThreads = ByteIOConstants.numThreads; // Denotes the number of parallel-threads
	private RandomByteIOTransferer[] transferer; // Each transferer denotes a unique end-point

	/**
	 * Create a new RandomByteIO input stream for a given endpoint and transfer protocol.
	 * 
	 * @param source
	 *            The source ByteIO to read bytes from.
	 * @param desiredTransferProtocol
	 *            The desired transfer protocol to use when reading bytes.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public RandomByteIOInputStream(EndpointReferenceType source, URI desiredTransferProtocol) throws RemoteException,
		IOException
	{

		if (numThreads <= 1)
			isMultiThreaded = false;

		if (!isMultiThreaded) {
			RandomByteIOPortType clientStub = ClientUtils.createProxy(RandomByteIOPortType.class, source);
			RandomByteIOTransfererFactory factory = new RandomByteIOTransfererFactory(clientStub);
			transferer = new RandomByteIOTransferer[1];
			transferer[0] = factory.createRandomByteIOTransferer(desiredTransferProtocol);
		}

		else {
			// The number of threads is >=2 (i.e) we will have parallelism

			RandomByteIOPortType[] clientStub = new RandomByteIOPortType[numThreads];
			RandomByteIOTransfererFactory[] factory = new RandomByteIOTransfererFactory[numThreads];
			transferer = new RandomByteIOTransferer[numThreads];

			for (int lcv = 0; lcv < numThreads; lcv++) {
				clientStub[lcv] = ClientUtils.createProxy(RandomByteIOPortType.class, source);
				factory[lcv] = new RandomByteIOTransfererFactory(clientStub[lcv]);
				transferer[lcv] = factory[lcv].createRandomByteIOTransferer(desiredTransferProtocol);
			}

		}
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

		if (!isMultiThreaded) {
			byte[] data = transferer[0].read(_offset, length, 1, 0);
			_offset += data.length;
			return data;
		}

		else {

			int threadBlkReadSize = (length / numThreads);
			// denotes block-size which each thread reads

			Thread[] thread = new Thread[numThreads];
			FastRead[] fr = new FastRead[numThreads];

			CountDownLatch cdl = new CountDownLatch(numThreads);
			FillerAndChecker fac = new FillerAndChecker(cdl, length);

			int subLength = 0;

			for (int i = 0; i < numThreads - 1; ++i) {
				fr[i] = new FastRead(transferer[i], _offset + subLength, threadBlkReadSize, fac, i, threadBlkReadSize);
				subLength += threadBlkReadSize;
				thread[i] = new Thread(fr[i]);
			}

			// Handles the case when length is not a perfect multiple of the number of threads
			fr[numThreads - 1] = new FastRead(transferer[numThreads - 1], _offset + subLength, threadBlkReadSize
				+ (length % numThreads), fac, numThreads - 1, threadBlkReadSize);

			thread[numThreads - 1] = new Thread(fr[numThreads - 1]);

			for (int i = 0; i < numThreads; ++i)
				thread[i].start();

			try {
				fac.await();
			}

			catch (InterruptedException ie) {
				throw new IOException(ie);
			}

			if (fac.isErrorFlag())
				throw new IOException(fac.getThreadFailCause());

			int lastFilledBufferIndex = fac.getLastFilledBufferIndex();

			if (lastFilledBufferIndex != -1) // I have fetched some data
			{

				if (lastFilledBufferIndex != (length - 1))
				// I have fetched only a subset of the requested amount!
				{
					byte[] temp_data = new byte[lastFilledBufferIndex + 1];
					System.arraycopy(fac.getData(), 0, temp_data, 0, lastFilledBufferIndex + 1);
					_offset += temp_data.length;
					return temp_data;
				}

				else // I have fetched the requested amount !
				{
					_offset += fac.getData().length;
					return fac.getData();
				}
			}

			else // Attempt to read 0 bytes
			{
				byte[] data = new byte[0];
				_offset += data.length;
				return data;
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
	 * A convenience method for creating a buffered stream (using the current transferer's preferred
	 * buffering size) from this input stream.
	 * 
	 * @return The newly created buffered input stream.
	 */
	public BufferedInputStream createPreferredBufferedStream()
	{
		return new BufferedInputStream(this, transferer[0].getPreferredReadSize());
	}

}