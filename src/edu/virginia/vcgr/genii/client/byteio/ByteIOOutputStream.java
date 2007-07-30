package edu.virginia.vcgr.genii.client.byteio;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactory;
import edu.virginia.vcgr.genii.client.byteio.xfer.TransferUtils;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

/**
 * An implementation of the java.io.OutputStream class which treats ByteIO resources (be
 * they Random or Streamable) as a standard stream instance.
 * 
 * @author Mark Morgan
 */
public class ByteIOOutputStream extends OutputStream
{
	static private Log _logger = LogFactory.getLog(ByteIOOutputStream.class);
	private OutputStream _handler;
	
	/**
	 * Create a new output stream connected to the given target EPR.  Reguardless of
	 * whether the endpoint given represents a RandomByteIO endpoint, a StreamableByteIO
	 * endpoint, or even a StreamableByteIO Factory type, the correct linkage is
	 * established.  If a streamable byteio factory is used to create a new stream, then
	 * that stream is terminated when this output stream instance is closed.
	 * 
	 * @param epr The endpoint which the instance will write data to.
	 */
	public ByteIOOutputStream(EndpointReferenceType epr)
		throws RemoteException, IOException, ConfigurationException
	{
		TransferUtils transUtils = new TransferUtils(epr);
		int blockSize = transUtils.getPreferredBlockSize();
		URI transType = transUtils.getPreferredTransferType();
		
		_logger.debug("Selecting transfer mechanism \"" + transType + "\".");
		
		TypeInformation ti = new TypeInformation(epr);
		if (ti.isRByteIO())
			_handler = new RandomByteIOOutputStream(epr, transType);
		else if (ti.isSByteIO())
			_handler = new StreamableByteIOOutputStream(epr, transType);
		else if (ti.isSByteIOFactory())
		{
			StreamableByteIOFactory factory = ClientUtils.createProxy(
				StreamableByteIOFactory.class, epr);
			_handler = new StreamableByteIOOutputStream(
				factory.openStream(null).getEndpoint(), transType);
		} else
			throw new IOException("Endpoint does not appear to support ByteIO.");
		
		_handler = new BufferedOutputStream(_handler, blockSize);
	}
	
	/**
	 * Create a new output stream connected to the given target EPR.  Reguardless of
	 * whether the endpoint given represents a RandomByteIO endpoint, a StreamableByteIO
	 * endpoint, or even a StreamableByteIO Factory type, the correct linkage is
	 * established.  Further, if the target indicated indicates a non-existant entry,
	 * then a new RandomByteIO will be created (if possible) with that name.
	 * If a streamable byteio factory is used to create a new stream, then
	 * that stream is terminated when this output stream instance is closed.
	 * 
	 * @param path The endpoint which the instance will write data to.
	 */
	public ByteIOOutputStream(RNSPath path)
		throws RemoteException, IOException, ConfigurationException, RNSException
	{
		if (!path.exists())
			path.createFile();
		
		EndpointReferenceType epr = path.getEndpoint();
		TypeInformation ti = new TypeInformation(epr);
		
		TransferUtils transUtils = new TransferUtils(epr);
		int blockSize = transUtils.getPreferredBlockSize();
		URI transType = transUtils.getPreferredTransferType();
		
		_logger.debug("Selecting transfer mechanism \"" + transType + "\".");
		
		if (ti.isRByteIO())
			_handler = new RandomByteIOOutputStream(epr, transType);
		else if (ti.isSByteIO())
			_handler = new StreamableByteIOOutputStream(epr, transType);
		else if (ti.isSByteIOFactory())
		{
			StreamableByteIOFactory factory = ClientUtils.createProxy(
				StreamableByteIOFactory.class, epr);
			_handler = new StreamableByteIOOutputStream(
				factory.openStream(null).getEndpoint(), transType);
		} else
			throw new IOException("Endpoint does not appear to support ByteIO.");
		
		_handler = new BufferedOutputStream(_handler, blockSize);
	}
	
	@Override
	public void write(int b) throws IOException
	{
		_handler.write(b);
	}
	
	public void write(byte []b) throws IOException
	{
		_handler.write(b);
	}
	
	public void write(byte []b, int off, int len) throws IOException
	{
		_handler.write(b, off, len);
	}
	
	public void close() throws IOException
	{
		_handler.close();
	}
	
	public void flush() throws IOException
	{
		_handler.flush();
	}
	
	public void setTransferMechanism(URI transferMechanism)
	{
		((ByteIOStream)_handler).setTransferMechanism(transferMechanism);
	}
}
