package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

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

class RandomByteIOOpenFile extends OperatorBasedOpenFile
{
	static private BasicFileOperator createOperator(EndpointReferenceType target) 
		throws ResourceException, GenesisIISecurityException, RemoteException, 
			IOException
	{
		RandomByteIOTransferer transferer =
			RandomByteIOTransfererFactory.createRandomByteIOTransferer(
				ClientUtils.createProxy(RandomByteIOPortType.class, target));
		
		return new BasicFileOperator(
			ByteIOBufferLeaser.leaser(transferer.getTransferProtocol()),
			new ReadResolverImpl(transferer),
			new WriteResolverImpl(transferer),
			new AppendResolverImpl(transferer), false);
	}
	
	RandomByteIOOpenFile(String[] path, EndpointReferenceType target,
		boolean canRead, boolean canWrite, boolean isAppend)
			throws ResourceException, GenesisIISecurityException, 
				RemoteException, IOException
	{
		super(path, createOperator(target),
			canRead, canWrite, isAppend);
	}
	
	static private class ReadResolverImpl implements ReadResolver
	{
		private RandomByteIOTransferer _transferer;
		
		private ReadResolverImpl(RandomByteIOTransferer transferer)
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
	
	static private class WriteResolverImpl implements WriteResolver
	{
		private RandomByteIOTransferer _transferer;
		
		private WriteResolverImpl(RandomByteIOTransferer transferer)
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
	
	static private class AppendResolverImpl implements AppendResolver
	{
		private RandomByteIOTransferer _transferer;
		
		private AppendResolverImpl(RandomByteIOTransferer transferer)
		{
			_transferer = transferer;
		}
		
		@Override
		public void append(ByteBuffer source) throws IOException
		{
			_transferer.append(source);
		}
	}
}