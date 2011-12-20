package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;

import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.buffer.AppendResolver;
import edu.virginia.vcgr.genii.client.byteio.buffer.BasicFileOperator;
import edu.virginia.vcgr.genii.client.byteio.buffer.ByteIOBufferLeaser;
import edu.virginia.vcgr.genii.client.byteio.buffer.ReadResolver;
import edu.virginia.vcgr.genii.client.byteio.buffer.WriteResolver;
import edu.virginia.vcgr.genii.client.byteio.parallelByteIO.FastRead;
import edu.virginia.vcgr.genii.client.byteio.parallelByteIO.FillerAndChecker;
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
		
		//ak3ka's additions for parallel fuse IO
		int numThreads = ByteIOConstants.numThreads;
		RandomByteIOTransferer rbit[] = new RandomByteIOTransferer[numThreads];
        for(int i=0; i<numThreads; ++i)
        	rbit[i] = RandomByteIOTransfererFactory.createRandomByteIOTransferer(
                      ClientUtils.createProxy(RandomByteIOPortType.class, target));

        return new BasicFileOperator(
                        ByteIOBufferLeaser.leaser(rbit[0].getTransferProtocol()),
                        new ReadResolverImpl(rbit),
                        new WriteResolverImpl(rbit[0]),
                        new AppendResolverImpl(rbit[0]), false);		
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

		//ak3ka's additions for parallel fuse-read !
		
		 private RandomByteIOTransferer[] _transferer;

		 private ReadResolverImpl(RandomByteIOTransferer transferer[])
         {
                 _transferer = transferer;
         }

		
		@Override
		public void read(long fileOffset, ByteBuffer destination)
			throws IOException
		{
		
			int length = destination.remaining();
            int numThreads = ByteIOConstants.numThreads;
            int threadBlkReadSize=(length/numThreads);
			
            Thread[] thread = new Thread[numThreads];
            FastRead[] fr = new FastRead[numThreads];

            CountDownLatch cdl = new CountDownLatch(numThreads);

            FillerAndChecker fac = new FillerAndChecker(cdl, length);

            int subLength = 0;

            for(int i=0;i<numThreads-1; ++i)
            {
            	fr[i] = new FastRead(_transferer[i], fileOffset + subLength,
                                threadBlkReadSize, fac, i, threadBlkReadSize);
            	subLength += threadBlkReadSize;
                thread[i]= new Thread(fr[i]);
            }

            fr[numThreads-1] = new FastRead(_transferer[numThreads-1], fileOffset + subLength,
                                        threadBlkReadSize + (length % numThreads), fac, numThreads-1,
                                        threadBlkReadSize);

            thread[numThreads - 1] = new Thread(fr[numThreads - 1] );

            for(int i=0; i<numThreads; ++i)
                thread[i].start();
            
            try
            {
            	fac.await();
            }
            
            catch (InterruptedException ie)
            {
            	throw new IOException(ie);
            }


            if(fac.isErrorFlag())
                throw new IOException(fac.getThreadFailCause());

            int lastFilledBufferIndex = fac.getLastFilledBufferIndex();

            if( lastFilledBufferIndex != -1)
            {

                
                if(lastFilledBufferIndex != (length -1 ))
                        //I have fetched only a subset of the requested amount!
                {
                	byte[] temp_data = new byte[lastFilledBufferIndex+1];
                    System.arraycopy(fac.getData() , 0, temp_data, 0, lastFilledBufferIndex+1);
                    destination.put(temp_data);                                           
                }
                
                else
                	destination.put(fac.getData());                       
                
              }

              else  // Attempt to read 0 bytes
              {
                   return;
              }
			
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