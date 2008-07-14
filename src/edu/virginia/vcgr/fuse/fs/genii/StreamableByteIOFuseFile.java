package edu.virginia.vcgr.fuse.fs.genii;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import org.ggf.sbyteio.StreamableByteIOPortType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import fuse.FuseException;

class StreamableByteIOFuseFile extends FuseFileCommon
{
	private StreamableByteIOPortType _portType;
	private StreamableByteIOTransferer _transferer;
	
	StreamableByteIOFuseFile(EndpointReferenceType target,
		GeniiFuseFileSystemContext fsContext, boolean read,
		boolean write, boolean append, Long truncateLength) 
			throws ResourceException, GenesisIISecurityException, 
				RemoteException, IOException
	{
		super(target, fsContext, read, write, append);
		
		_portType = ClientUtils.createProxy(
			StreamableByteIOPortType.class, target);
		_transferer = 
			StreamableByteIOTransfererFactory.createStreamableByteIOTransferer(
				_portType);
		
		// We have to ignore truncate requests on streambles.
	}
	
	@Override
	protected void appendImpl(ByteBuffer buffer) throws FuseException
	{
		try
		{
			byte []data = new byte[buffer.remaining()];
			buffer.get(data);
			_transferer.seekWrite(SeekOrigin.SEEK_END, 0, data);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate(
				"Unable to write to streamable byteIO.", cause);
		}
	}

	@Override
	protected void readImpl(long offset, ByteBuffer buffer)
			throws FuseException
	{
		try
		{
			byte []data = _transferer.seekRead(
				SeekOrigin.SEEK_BEGINNING, offset, buffer.remaining());
			buffer.put(data, 0, data.length);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate(
				"Unable to read streamable byteIO.", cause);
		}
	}

	@Override
	protected void writeImpl(long offset, ByteBuffer buffer)
			throws FuseException
	{
		try
		{
			byte []data = new byte[buffer.remaining()];
			buffer.get(data);
			_transferer.seekWrite(SeekOrigin.SEEK_BEGINNING, offset, data);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate(
				"Unable to write to streamable byteIO.", cause);
		}
	}

	@Override
	public void flush() throws FuseException
	{
		// we auto-flush
	}

	@Override
	public void release() throws FuseException
	{
		try
		{
			_portType.destroy(new Destroy());
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate(
				"Unable to release Streamable byte io.", cause);
		}
	}
	
	@Override
	public void close() throws IOException
	{
		// do nothing
	}
}