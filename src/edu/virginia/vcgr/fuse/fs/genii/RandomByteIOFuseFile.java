package edu.virginia.vcgr.fuse.fs.genii;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fuse.exceptions.FuseBadFileHandleException;
import edu.virginia.vcgr.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.genii.client.byteio.buffer.AppendResolver;
import edu.virginia.vcgr.genii.client.byteio.buffer.BasicFileOperator;
import edu.virginia.vcgr.genii.client.byteio.buffer.ByteIOBufferLeaser;
import edu.virginia.vcgr.genii.client.byteio.buffer.ReadResolver;
import edu.virginia.vcgr.genii.client.byteio.buffer.WriteResolver;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import fuse.FuseException;

class RandomByteIOFuseFile extends FuseFileCommon
{
	private BasicFileOperator _operator;
	
	RandomByteIOFuseFile(EndpointReferenceType target, 
		GeniiFuseFileSystemContext fsContext,
		boolean read, boolean write, boolean append, Long truncateLength) 
			throws ResourceException, GenesisIISecurityException, 
				RemoteException, IOException
	{
		super(target, fsContext, read, write, append);
		
		RandomByteIOTransferer transferer = 
			RandomByteIOTransfererFactory.createRandomByteIOTransferer(
			ClientUtils.createProxy(RandomByteIOPortType.class, target));
		
		_operator = new BasicFileOperator(
			ByteIOBufferLeaser.leaser(), 
			new RandomByteIOReadResolver(transferer),
			new RandomByteIOWriteResolver(transferer),
			new RandomByteIOAppendResolver(transferer), false);
		
		if (truncateLength != null)
			_operator.truncate(truncateLength.longValue());
	}
	
	@Override
	protected void readImpl(long offset, ByteBuffer buffer) throws FuseException
	{
		if (_operator == null)
			throw new FuseBadFileHandleException("File is closed.");
		
		try
		{
			while (buffer.hasRemaining())
			{
				byte []data = new byte[buffer.remaining()];
				int read = _operator.read(offset, data, 0, data.length);
				if (read <= 0)
					return;
				buffer.put(data, 0, read);
				offset += read;
			}
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
		if (_operator == null)
			throw new FuseBadFileHandleException("File is closed.");
	
		try
		{
			byte []data = new byte[buffer.remaining()];
			buffer.get(data);
			_operator.write(offset, data, 0, data.length);
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
		if (_operator == null)
			throw new FuseBadFileHandleException("File is closed.");
	
		try
		{
			byte []data = new byte[buffer.remaining()];
			buffer.get(data);
			_operator.append(data, 0, data.length);
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
		try
		{
			if (_operator != null)
				_operator.close();
			_operator = null;
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate(
				"Unable to append random byteIO.", cause);
		}
	}
	
	@Override
	synchronized public void close() throws IOException
	{
		if (_operator != null)
			_operator.close();
		_operator = null;
	}
	
	@Override
	public void flush() throws FuseException
	{
		if (_operator == null)
			throw new FuseBadFileHandleException("File is closed.");
	
		try
		{
			_operator.flush();
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate(
				"Unable to append random byteIO.", cause);
		}
	}
	
	static private class RandomByteIOWriteResolver implements WriteResolver
	{
		private RandomByteIOTransferer _transferer;
		
		private RandomByteIOWriteResolver(RandomByteIOTransferer transferer)
		{
			_transferer = transferer;
		}
		
		@Override
		public void truncate(long offset) throws IOException
		{
			_transferer.truncAppend(offset, new byte[0]);
		}

		@Override
		public void write(long fileOffset, byte[] source, int sourceOffset,
				int length) throws IOException
		{
			byte []data = new byte[length];
			System.arraycopy(source, sourceOffset, data, 0, length);
			_transferer.write(fileOffset, length, 0, data);
		}
	}
	
	static private class RandomByteIOAppendResolver implements AppendResolver
	{
		private RandomByteIOTransferer _transferer;
		
		private RandomByteIOAppendResolver(RandomByteIOTransferer transferer)
		{
			_transferer = transferer;
		}

		@Override
		public void append(byte[] data, int start, int length)
				throws IOException
		{
			byte []tmpData = new byte[length];
			System.arraycopy(data, start, tmpData, 0, length);
			_transferer.append(tmpData);
		}
	}
	
	static private class RandomByteIOReadResolver implements ReadResolver
	{
		private RandomByteIOTransferer _transferer;
		
		private RandomByteIOReadResolver(RandomByteIOTransferer transferer)
		{
			_transferer = transferer;
		}

		@Override
		public int read(long fileOffset, byte[] destination,
				int destinationOffset, int length) throws IOException
		{
			byte []data = _transferer.read(fileOffset, length, 1, 0);
			System.arraycopy(data, 0, destination, destinationOffset, 
				data.length);
			return data.length;
		}
	}
}