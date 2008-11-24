package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import org.ggf.sbyteio.StreamableByteIOPortType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

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

class StreamableByteIOOpenFile extends OperatorBasedOpenFile
{
	static private BasicFileOperator createOperator(
		StreamableByteIOPortType portType) throws ResourceException, 
			GenesisIISecurityException, RemoteException, IOException
	{
		StreamableByteIOTransferer transferer =
			StreamableByteIOTransfererFactory.createStreamableByteIOTransferer(
				portType);
		
		return new BasicFileOperator(
			ByteIOBufferLeaser.leaser(transferer.getTransferProtocol()),
			new ReadResolverImpl(transferer),
			new WriteResolverImpl(transferer),
			new AppendResolverImpl(transferer), false);
	}

	private StreamableByteIOPortType _destroyer = null;
	
	StreamableByteIOOpenFile(boolean wasCreated, 
		StreamableByteIOPortType portType,
		boolean canRead, boolean canWrite, boolean isAppend)
			throws ResourceException, GenesisIISecurityException, 
				RemoteException, IOException
	{
		super(createOperator(portType), canRead, canWrite, isAppend);
		
		if (wasCreated)
			_destroyer = portType;
	}
	
	StreamableByteIOOpenFile(boolean wasCreated, EndpointReferenceType target,
		boolean canRead, boolean canWrite, boolean isAppend)
			throws ResourceException, GenesisIISecurityException, 
				RemoteException, IOException
	{
		this(wasCreated, ClientUtils.createProxy(
			StreamableByteIOPortType.class, target),
			canRead, canWrite, isAppend);
	}
	
	@Override
	protected void closeImpl() throws IOException
	{
		super.closeImpl();
		
		if (_destroyer != null)
			_destroyer.destroy(new Destroy());
	}

	static private class ReadResolverImpl implements ReadResolver
	{
		private StreamableByteIOTransferer _transferer;
		
		private ReadResolverImpl(StreamableByteIOTransferer transferer)
		{
			_transferer = transferer;
		}
		
		@Override
		public void read(long fileOffset, ByteBuffer destination)
			throws IOException
		{
			_transferer.seekRead(SeekOrigin.SEEK_BEGINNING,
				fileOffset, destination);
		}
	}
	
	static private class WriteResolverImpl implements WriteResolver
	{
		private StreamableByteIOTransferer _transferer;
		
		private WriteResolverImpl(StreamableByteIOTransferer transferer)
		{
			_transferer = transferer;
		}
		
		@Override
		public void truncate(long offset) throws IOException
		{
			// We have to ignore -- can't truncate streams.
		}
		
		@Override
		public void write(long fileOffset, ByteBuffer source)
			throws IOException
		{
			_transferer.seekWrite(SeekOrigin.SEEK_BEGINNING,
				fileOffset, source);
		}
	}
	
	static private class AppendResolverImpl implements AppendResolver
	{
		private StreamableByteIOTransferer _transferer;
		
		private AppendResolverImpl(StreamableByteIOTransferer transferer)
		{
			_transferer = transferer;
		}
		
		@Override
		public void append(ByteBuffer source) throws IOException
		{
			_transferer.seekWrite(SeekOrigin.SEEK_END, 0, source);
		}
	}
}