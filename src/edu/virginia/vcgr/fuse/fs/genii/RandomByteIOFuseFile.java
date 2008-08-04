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
			ByteIOBufferLeaser.leaser(transferer.getTransferProtocol()), 
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
				int start = buffer.position();
				_operator.read(offset, buffer);
				int read = buffer.position() - start;
				if (read <= 0)
					return;
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
			_operator.write(offset, buffer);
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
			_operator.append(buffer);
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
		public void write(long fileOffset, ByteBuffer source)
				throws IOException
		{
			_transferer.write(fileOffset, source);
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
		public void append(ByteBuffer source) throws IOException
		{
			_transferer.append(source);
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
		public void read(long fileOffset, ByteBuffer destination)
				throws IOException
		{
			_transferer.read(fileOffset, destination);
		}
	}
}