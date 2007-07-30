package edu.virginia.vcgr.genii.client.byteio;

import java.io.IOException;
import java.io.OutputStream;
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
import edu.virginia.vcgr.genii.client.resource.ResourceException;

class StreamableByteIOOutputStream extends OutputStream
{
	private StreamableByteIOPortType _stub;
	private ISByteIOTransferer _transferer;
	
	StreamableByteIOOutputStream(EndpointReferenceType epr, URI xferType)
		throws ResourceException, ConfigurationException, RemoteException
	{
		_stub = ClientUtils.createProxy(StreamableByteIOPortType.class, epr);

		if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI))
			_transferer = new DimeSByteIOTransferer(_stub);
		else if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_MTOM_URI))
			_transferer = new MTomSByteIOTransferer(_stub);
		else
			_transferer = new SimpleSByteIOTransferer(_stub);
	}
	
	@Override
	public void write(int b) throws IOException
	{
		_transferer.seekWrite(SeekOrigin.SEEK_CURRENT, 0, 
			new byte[] { (byte)b });
	}
	
	public void write(byte[] b) throws IOException
	{
		_transferer.seekWrite(SeekOrigin.SEEK_CURRENT, 0, b);
	}
	
	public void write(byte []b, int off, int len) throws IOException
	{
		byte []data = new byte[len];
		System.arraycopy(b, off, data, 0, len);
		_transferer.seekWrite(SeekOrigin.SEEK_CURRENT, 0, data);
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