package edu.virginia.vcgr.fuse.fs.genii;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import org.ggf.sbyteio.StreamableByteIOPortType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;
import edu.virginia.vcgr.genii.client.byteio.buffer.AppendResolver;
import edu.virginia.vcgr.genii.client.byteio.buffer.BasicFileOperator;
import edu.virginia.vcgr.genii.client.byteio.buffer.ByteIOBufferLeaser;
import edu.virginia.vcgr.genii.client.byteio.buffer.ReadResolver;
import edu.virginia.vcgr.genii.client.byteio.buffer.WriteResolver;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import fuse.FuseException;

class StreamableByteIOFuseFile extends FuseFileCommon
{
	private StreamableByteIOPortType _portType;
	private BasicFileOperator _operator;
	
	StreamableByteIOFuseFile(EndpointReferenceType target,
		GeniiFuseFileSystemContext fsContext, boolean read,
		boolean write, boolean append, Long truncateLength) 
			throws ResourceException, GenesisIISecurityException, 
				RemoteException, IOException
	{
		super(target, fsContext, read, write, append);
		
		_portType = ClientUtils.createProxy(
			StreamableByteIOPortType.class, target);
		StreamableByteIOTransferer transferer = 
			StreamableByteIOTransfererFactory.createStreamableByteIOTransferer(
				_portType);
		
		_operator = new BasicFileOperator(
			ByteIOBufferLeaser.leaser(),
			new StreamableReadResolver(transferer),
			new StreamableWriteResolver(transferer),
			new StreamableAppendResolver(transferer), false);
		
		// We have to ignore truncate requests on streambles.
	}
	
	@Override
	protected void appendImpl(ByteBuffer buffer) throws FuseException
	{
		try
		{
			byte []data = new byte[buffer.remaining()];
			buffer.get(data);
			_operator.append(data, 0, data.length);
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
			while (buffer.hasRemaining())
			{
				byte []data = new byte[buffer.remaining()];
				int read = _operator.read(offset, data, 0, data.length);
				buffer.put(data, 0, read);
			}
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
			_operator.write(offset, data, 0, data.length);
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
		try
		{
			_operator.flush();
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate(
				"Unable to flush streamable byteIO.", cause);
		}
	}

	@Override
	public void release() throws FuseException
	{
		try
		{
			_operator.close();
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
		_operator.close();
		_portType.destroy(new Destroy());
	}
	
	static private class StreamableReadResolver implements ReadResolver
	{
		private StreamableByteIOTransferer _transferer;
		
		private StreamableReadResolver(StreamableByteIOTransferer transferer)
		{
			_transferer = transferer;
		}

		@Override
		public int read(long fileOffset, byte[] destination,
				int destinationOffset, int length) throws IOException
		{
			byte []data = _transferer.seekRead(SeekOrigin.SEEK_BEGINNING, 
				fileOffset, length);
			System.arraycopy(data, 0, destination, destinationOffset, length);
			return data.length;
		}
	}
	
	static private class StreamableWriteResolver implements WriteResolver
	{
		private StreamableByteIOTransferer _transferer;
		
		private StreamableWriteResolver(StreamableByteIOTransferer transferer)
		{
			_transferer = transferer;
		}

		@Override
		public void truncate(long offset) throws IOException
		{
			// We have to ignore -- can't truncate streams.
		}

		@Override
		public void write(long fileOffset, byte[] source, int sourceOffset,
				int length) throws IOException
		{
			byte []data = new byte[length];
			System.arraycopy(source, sourceOffset, data, 0, length);
			_transferer.seekWrite(SeekOrigin.SEEK_BEGINNING, fileOffset, data);
		}
	}
	
	static private class StreamableAppendResolver implements AppendResolver
	{
		private StreamableByteIOTransferer _transferer;
		
		private StreamableAppendResolver(StreamableByteIOTransferer transferer)
		{
			_transferer = transferer;
		}

		@Override
		public void append(byte[] data, int start, int length)
				throws IOException
		{
			byte []tmpData = new byte[length];
			System.arraycopy(data, start, tmpData, 0, length);
			_transferer.seekWrite(SeekOrigin.SEEK_END, 0, tmpData);
		}
	}
}