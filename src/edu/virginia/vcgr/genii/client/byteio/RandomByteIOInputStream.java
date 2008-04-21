package edu.virginia.vcgr.genii.client.byteio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;

/**
 * An implementation of the standard Java Input stream that reads
 * from remote Random ByteIO resources.
 * 
 * @author mmm2a
 */
public class RandomByteIOInputStream extends InputStream
{
	/* The transferer being used by this stream. */
	private RandomByteIOTransferer _transferer;
	
	/* The current offset within the remote random byteio resource */
	private long _offset = 0L;
	
	/**
	 * Create a new RandomByteIO input stream for a given endpoint and
	 * transfer protocol.
	 * 
	 * @param source The source ByteIO to read bytes from.
	 * @param desiredTransferProtocol The desired transfer protocol to use when
	 * reading bytes.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public RandomByteIOInputStream(EndpointReferenceType source,
		URI desiredTransferProtocol)
			throws ConfigurationException, RemoteException
	{
		RandomByteIOPortType clientStub = ClientUtils.createProxy(
			RandomByteIOPortType.class, source);
		RandomByteIOTransfererFactory factory = 
			new RandomByteIOTransfererFactory(clientStub);
		_transferer = factory.createRandomByteIOTransferer(desiredTransferProtocol);
	}
	
	/**
	 * Create a new RandomByteIO input stream for a given endpoint.
	 * 
	 * @param source The source ByteIO to read bytes from.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public RandomByteIOInputStream(EndpointReferenceType source)
		throws ConfigurationException, RemoteException
	{
		this(source, null);
	}
	
	/**
	 * A convenience method for reading a certain number of bytes from
	 * the target ByteIO.
	 * 
	 * @param length The number of bytes to read.
	 * 
	 * @return The block of data (if any) just read.
	 * 
	 * @throws IOException
	 */
	private byte[] read(int length) throws IOException
	{
		byte []data = _transferer.read(_offset, length, 1, 0);
		_offset += data.length;
		
		return data;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException
	{
		byte []data = read(1);
		if (data.length == 1)
			return data[0];
		
		return -1;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte []b) throws IOException
	{
		byte []data = read(b.length);
		System.arraycopy(data, 0, b, 0, data.length);
		return (data.length == 0) ? -1 : data.length;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte []b, int off, int len) throws IOException
	{
		byte []data = read(len);
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
	 * A convenience method for creating a buffered stream (using the
	 * current transferer's preferred buffering size) from this input stream.
	 * 
	 * @return The newly created buffered input stream.
	 */
	public BufferedInputStream createPreferredBufferedStream()
	{
		return new BufferedInputStream(
			this, _transferer.getPreferredReadSize());
	}
}