package edu.virginia.vcgr.genii.client.nativeq;

import java.io.Closeable;

public interface NativeQueueConnection extends Closeable
{
	public FactoryResourceAttributes describe() throws NativeQueueException;
	
	public JobToken submit(ApplicationDescription application)
			throws NativeQueueException;
	public NativeQueueState getStatus(JobToken token)
		throws NativeQueueException;
	public void cancel(JobToken token) throws NativeQueueException;
	public int getExitCode(JobToken token) throws NativeQueueException;
}