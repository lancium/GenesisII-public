package edu.virginia.vcgr.genii.client.gfs;

import java.util.concurrent.Semaphore;
/**
 * 	 This class implements the opposite of the classic readers/writers. We allow as many writers at a time, 
	 but when a readers comes we block it until all of the writes have flushed to the target ByteIO
 * @author coder
 *
 */
public class writersReaders {

	private Semaphore mutex;
	private Semaphore waitq;
	private Semaphore throttle;
	int numReaders;
	int numWriters;
	
	public writersReaders() {
		numReaders=0;
		numWriters=0;
		mutex = new Semaphore(1);
		waitq = new Semaphore(0);
		throttle = new Semaphore(0);
	}
	
	public void startWrite() {
		mutex.acquireUninterruptibly();
		numWriters++;
		//System.err.println("Acquiring numWriters = " + numWriters);
		mutex.release();			
	}
	
	public void waitForWritersToComplete() {
		startRead();
		
	}
	public void startRead() {
		mutex.acquireUninterruptibly();
		//System.err.println("Reading numWriters = " + numWriters + ", Readers =" + numReaders);

		if (numWriters > 0) {
			numReaders++;
			mutex.release();
			waitq.acquireUninterruptibly();
			throttle.release();
			return;
		}
		mutex.release();
	}
	
	public void endWrite() {
		mutex.acquireUninterruptibly();
		numWriters--;
		//System.err.println("Releasing numWriters = " + numWriters);
		if (numWriters==0){
			while (numReaders > 0 ) {
				waitq.release();
				throttle.acquireUninterruptibly();
				numReaders--;
			}
		}
		mutex.release();
	}
}