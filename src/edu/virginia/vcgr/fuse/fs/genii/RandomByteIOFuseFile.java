package edu.virginia.vcgr.fuse.fs.genii;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fuse.exceptions.FuseBadFileHandleException;
import edu.virginia.vcgr.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import fuse.FuseException;

class RandomByteIOFuseFile extends FuseFileCommon
{
	private RandomByteIOTransferer _transferer;
	
	RandomByteIOFuseFile(EndpointReferenceType target, 
		GeniiFuseFileSystemContext fsContext,
		boolean read, boolean write, boolean append, Long truncateLength) 
			throws ResourceException, GenesisIISecurityException, 
				RemoteException, IOException
	{
		super(target, fsContext, read, write, append);
		
		_transferer = 
			RandomByteIOTransfererFactory.createRandomByteIOTransferer(
			ClientUtils.createProxy(RandomByteIOPortType.class, target));
		
		if (truncateLength != null)
			_transferer.truncAppend(truncateLength.longValue(), new byte[0]);
	}
	
	@Override
	protected void readImpl(long offset, ByteBuffer buffer) throws FuseException
	{
		if (_transferer == null)
			throw new FuseBadFileHandleException("File is closed.");
		
		try
		{
			byte []data = 
				_transferer.read(offset, buffer.remaining(), 1, 0);
			buffer.put(data);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate(
				"Unable to read random byteIO.", cause);
		}
	}

	@Override
	protected void writeImpl(long offset, ByteBuffer buffer) throws FuseException
	{
		if (_transferer == null)
			throw new FuseBadFileHandleException("File is closed.");
	
		try
		{
			byte []data = new byte[buffer.remaining()];
			buffer.get(data);
			_transferer.write(offset, data.length, 0, data);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate(
				"Unable to write random byteIO.", cause);
		}
	}

	@Override
	protected void appendImpl(ByteBuffer buffer) throws FuseException
	{
		if (_transferer == null)
			throw new FuseBadFileHandleException("File is closed.");
	
		try
		{
			byte []data = new byte[buffer.remaining()];
			buffer.get(data);
			_transferer.append(data);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate(
				"Unable to append random byteIO.", cause);
		}
	}
	
	@Override
	synchronized public void release() throws FuseException
	{
		// do nothing
	}
	
	@Override
	synchronized public void close() throws IOException
	{
		// do nothing
	}
	
	@Override
	public void flush() throws FuseException
	{
		// do nothing
	}
}