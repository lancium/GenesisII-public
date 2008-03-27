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

public class RandomByteIOInputStream extends InputStream
{
	private RandomByteIOTransferer _transferer;
	private long _offset = 0L;
	
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
	
	public RandomByteIOInputStream(EndpointReferenceType source)
		throws ConfigurationException, RemoteException
	{
		this(source, null);
	}
	
	private byte[] read(int length) throws IOException
	{
		byte []data = _transferer.read(_offset, length, 1, 0);
		_offset += data.length;
		
		return data;
	}
	
	public int read() throws IOException
	{
		byte []data = read(1);
		if (data.length == 1)
			return data[0];
		
		return -1;
	}
	
	public int read(byte []b) throws IOException
	{
		byte []data = read(b.length);
		System.arraycopy(data, 0, b, 0, data.length);
		return (data.length == 0) ? -1 : data.length;
	}
	
	public int read(byte []b, int off, int len) throws IOException
	{
		byte []data = read(len);
		System.arraycopy(data, 0, b, off, data.length);
		return (data.length == 0) ? -1 : data.length;
	}
	
	@Override
	public long skip(long n)
	{
		_offset += n;
		return _offset;
	}
	
	public BufferedInputStream createPreferredBufferedStream()
	{
		return new BufferedInputStream(
			this, _transferer.getPreferredReadSize());
	}
}