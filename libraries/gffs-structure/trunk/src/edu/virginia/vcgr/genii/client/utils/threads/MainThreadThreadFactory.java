package edu.virginia.vcgr.genii.client.utils.threads;

import java.util.LinkedList;
import java.util.concurrent.ThreadFactory;

public class MainThreadThreadFactory implements ThreadFactory
{
	private class ThreadRequest
	{
		private Runnable _runnable;
		private Thread _thread = null;

		public ThreadRequest(Runnable runnable)
		{
			_runnable = runnable;
		}

		public Runnable getRunnable()
		{
			return _runnable;
		}

		public void setThread(Thread thread)
		{
			_thread = thread;
		}

		public Thread getThread()
		{
			return _thread;
		}
	}

	private LinkedList<ThreadRequest> _requests = new LinkedList<ThreadRequest>();

	public MainThreadThreadFactory()
	{
		Thread th = new Thread(new ThreadFactoryWorker());
		th.setDaemon(false);
		th.setName("Thread Factory");
		th.start();
	}

	public Thread newThread(Runnable arg0)
	{
		ThreadRequest request = new ThreadRequest(arg0);
		synchronized (request) {
			synchronized (_requests) {
				_requests.addLast(request);
				_requests.notify();
			}

			try {
				request.wait();
				return request.getThread();
			} catch (InterruptedException ie) {
				Thread.interrupted();
				throw new RuntimeException("Thread interrupted.", ie);
			}
		}
	}

	private class ThreadFactoryWorker implements Runnable
	{
		public void run()
		{
			ThreadRequest request;

			while (true) {
				try {
					synchronized (_requests) {
						while (_requests.isEmpty())
							_requests.wait();

						request = _requests.removeFirst();
					}

					synchronized (request) {
						request.setThread(new Thread(request.getRunnable()));
						request.notify();
					}
				} catch (InterruptedException ie) {
					Thread.interrupted();
				}
			}
		}
	}
}