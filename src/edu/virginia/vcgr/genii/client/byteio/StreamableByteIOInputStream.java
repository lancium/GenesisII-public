package edu.virginia.vcgr.genii.client.byteio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.xfer.ISByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.dime.DimeSByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.mtom.MTomSByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.simple.SimpleSByteIOTransferer;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

class StreamableByteIOInputStream extends InputStream
{
	private StreamableByteIOPortType _stub;
	private ISByteIOTransferer _transferer;
	private long _nextSeek = 0;

	StreamableByteIOInputStream(EndpointReferenceType epr, URI xferType)
		throws ConfigurationException, RemoteException
	{
		_stub = ClientUtils.createProxy(StreamableByteIOPortType.class, epr);

		if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI))
			_transferer = new DimeSByteIOTransferer(_stub);
		else if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_MTOM_URI))
			_transferer = new MTomSByteIOTransferer(_stub);
		else
			_transferer = new SimpleSByteIOTransferer(_stub);
	}
	
	StreamableByteIOInputStream(RNSPath path)
		throws IOException, RemoteException, ConfigurationException, RNSException
	{
		if (!path.exists())
			throw new FileNotFoundException("Couldn't find file \"" +
				path.pwd() + "\".");
		
		if (!path.isFile())
			throw new RNSException("Path \"" + path.pwd() +
				"\" does not represent a file.");
		
		_transferer = new SimpleSByteIOTransferer(
			ClientUtils.createProxy(StreamableByteIOPortType.class, path.getEndpoint()));
	}
	
	@Override
	public int read() throws IOException
	{
		byte []data = _transferer.seekRead(SeekOrigin.SEEK_CURRENT, _nextSeek, 1);
		_nextSeek = 0;
		if (data.length == 1)
			return data[0];
		if (data.length == 0)
			return (_transferer.endOfStream() ? -1 : 0);
		return -1;
	}
	
	public int read(byte []b) throws IOException
	{
		byte []data = _transferer.seekRead(SeekOrigin.SEEK_CURRENT, _nextSeek, b.length);
		_nextSeek = 0;
		System.arraycopy(data, 0, b, 0, data.length);
		if (data.length == 0)
			return (_transferer.endOfStream() ? -1 : 0);
		return data.length;
	}
	
	public int read(byte []b, int off, int len) throws IOException
	{
		byte []data = _transferer.seekRead(SeekOrigin.SEEK_CURRENT, _nextSeek, len);
		_nextSeek = 0;
		System.arraycopy(data, 0, b, off, data.length);
		if (data.length == 0)
			return (_transferer.endOfStream() ? -1 : 0);
		return data.length;
	}
	
	public long skip(long n) throws IOException
	{
		try
		{
			_nextSeek = n;
			return _transferer.position() + _nextSeek;
		}
		catch (RemoteException re)
		{
			throw new IOException(re.getLocalizedMessage());
		}
	}
	
	public void close() throws IOException
	{
		_transferer.close();
		_transferer = null;
	}
    
	public void setTransferMechanism(URI transferMechanism)
	{
		if (transferMechanism.equals(
			ByteIOConstants.TRANSFER_TYPE_DIME_URI))
			_transferer = new DimeSByteIOTransferer(_stub);
		else if (transferMechanism.equals(
			ByteIOConstants.TRANSFER_TYPE_MTOM_URI))
			_transferer = new MTomSByteIOTransferer(_stub);
		else
			_transferer = new SimpleSByteIOTransferer(_stub);
	}
}