package edu.virginia.vcgr.genii.client.byteio;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactory;
import edu.virginia.vcgr.genii.client.byteio.xfer.TransferUtils;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation of the java.io.InputStream class which treats ByteIO resources (be
 * they Random or Streamable) as a standard stream instance.
 * 
 * @author Mark Morgan
 */
public class ByteIOInputStream extends InputStream implements ByteIOStream
{
	static private Log _logger = LogFactory.getLog(ByteIOInputStream.class);
	private InputStream _handler;
	
	/**
	 * Create a new input stream connected to the given target EPR.  Reguardless of
	 * whether the endpoint given represents a RandomByteIO endpoint, a StreamableByteIO
	 * endpoint, or even a StreamableByteIO Factory type, the correct linkage is
	 * established.  If a streamable byteio factory is used to create a new stream, then
	 * that stream is terminated when this input stream instance is closed.
	 * 
	 * @param epr The endpoint which the instance will read data from.
	 */
	public ByteIOInputStream(EndpointReferenceType epr)
		throws RemoteException, IOException, ConfigurationException
	{
		TransferUtils transUtils = new TransferUtils(epr);
		int blockSize = transUtils.getPreferredBlockSize();
		URI transType = transUtils.getPreferredTransferType();
		
		_logger.debug("Selecting transfer mechanism \"" + transType + "\".");
		
		TypeInformation ti = new TypeInformation(epr);
		if (ti.isRByteIO())
			_handler = new RandomByteIOInputStream(epr, transType);
		else if (ti.isSByteIO())
			_handler = new StreamableByteIOInputStream(epr, transType);
		else if (ti.isSByteIOFactory())
		{
			StreamableByteIOFactory factory = ClientUtils.createProxy(
				StreamableByteIOFactory.class, epr);
			_handler = new StreamableByteIOInputStream(
				factory.openStream(null).getEndpoint(), transType);
		} else
			throw new IOException("Endpoint does not appear to support ByteIO.");
		
		_handler = new BufferedInputStream(_handler, blockSize);
	}
	
	/**
	 * Create a new input stream connected to the given target.  Reguardless of
	 * whether the endpoint given represents a RandomByteIO endpoint, a StreamableByteIO
	 * endpoint, or even a StreamableByteIO Factory type, the correct linkage is
	 * established.  If a streamable byteio factory is used to create a new stream, then
	 * that stream is terminated when this input stream instance is closed.
	 * 
	 * @param path The target which the instance will read data from.
	 */
	public ByteIOInputStream(RNSPath path)
		throws RemoteException, IOException, ConfigurationException, RNSException
	{
		if (!path.exists())
			throw new FileNotFoundException("Couldn't find file \"" +
				path.pwd() + "\".");

		EndpointReferenceType epr = path.getEndpoint();

		TransferUtils transUtils = new TransferUtils(epr);
		int blockSize = transUtils.getPreferredBlockSize();
		URI transType = transUtils.getPreferredTransferType();
		
		_logger.debug("Selecting transfer mechanism \"" + transType + "\".");
		
		TypeInformation ti = new TypeInformation(epr);
		if (ti.isRByteIO())
			_handler = new RandomByteIOInputStream(epr, transType);
		else if (ti.isSByteIO())
			_handler = new StreamableByteIOInputStream(epr, transType);
		else if (ti.isSByteIOFactory())
		{
			StreamableByteIOFactory factory = ClientUtils.createProxy(
				StreamableByteIOFactory.class, epr);
			_handler = new StreamableByteIOInputStream(
				factory.openStream(null).getEndpoint(), transType);
		} else
			throw new IOException("Endpoint does not appear to support ByteIO.");
		
		_handler = new BufferedInputStream(_handler, blockSize);
	}
	
	@Override
	public int read() throws IOException
	{
		return _handler.read();
	}
	
	public int read(byte []b) throws IOException
	{
		return _handler.read(b);
	}
	
	public int read(byte []b, int off, int len) throws IOException
	{
		return _handler.read(b, off, len);
	}
	
	public long skip(long bytes) throws IOException
	{
		return _handler.skip(bytes);
	}
	
	public void close() throws IOException
	{
		_handler.close();
	}
	
	public void setTransferMechanism(URI transferMechanism)
	{
		((ByteIOStream)_handler).setTransferMechanism(transferMechanism);
	}
	
	public boolean markSupported(){
		return _handler.markSupported();
	}
	
	public void mark(int readlimit) {
		_handler.mark(readlimit);
	}
	
	public void reset() throws IOException { 
		_handler.reset();
	}	
}