package edu.virginia.vcgr.genii.client.byteio.parallelByteIO;

import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;

public class FillerAndChecker
{
        private CountDownLatch cdl;		//Latch waiting for all threads to finish
        private byte[] data;	//contains contents of read
        private int lastFilledBufferIndex = -1;
        private boolean errorFlag = false; //flag denoting if any thread failed in its read
        private RemoteException threadFailCause; //cause of thread-failure

        public FillerAndChecker(CountDownLatch cdl, int length)
        {
                data = new byte[length];
                this.cdl = cdl;
        }

        public void await() throws InterruptedException
        {
                cdl.await();
        }

        //Each thread copies the sub-fetch into the global buffer
        
        public void copyFetch(byte[] temp_buffer, int threadID, int subBufferSize)
        {
        	int index = threadID * subBufferSize;
        	System.arraycopy(temp_buffer, 0, data, index , temp_buffer.length);
        	if(temp_buffer.length != 0)
                setLastFilledBufferIndex(index + temp_buffer.length - 1);
        }

    /* Method to aid in checking if fetch was satisfied completely, partially 
      or not satisfied at all  */
    
        synchronized private void setLastFilledBufferIndex(int index)
        {
        	if(index > lastFilledBufferIndex)
                lastFilledBufferIndex = index;
        }

        public void completed()
        {
            cdl.countDown();
        }

        public void setErrorFlag(RemoteException cause)
        {
            errorFlag = true;       //read has failed
            setThreadFailCause(cause); //the reason why the read failed.
        }



        public boolean isErrorFlag()
        {
        	return errorFlag;
        }

        public void setThreadFailCause(RemoteException threadFailCause)
        {
                this.threadFailCause = threadFailCause;
        }

        public RemoteException getThreadFailCause() 
        {
                return threadFailCause;
        }

        public int getLastFilledBufferIndex()
        {
                return lastFilledBufferIndex;
        }
        
        public byte[] getData()
        {
                return data;
        }

}
