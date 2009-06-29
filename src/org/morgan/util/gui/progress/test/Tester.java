package org.morgan.util.gui.progress.test;

import org.morgan.util.gui.progress.DefaultProgressNotifier;
import org.morgan.util.gui.progress.ProgressListener;
import org.morgan.util.gui.progress.ProgressMonitor;

public class Tester
{
	static private class ProgressListenerImpl 
		implements ProgressListener<String>
	{
		private boolean _finished = false;
		private String _value = null;
		
		public String get() throws InterruptedException
		{
			synchronized(this)
			{
				while (!_finished)
					wait();
			}
			
			return _value;
		}

		@Override
		public void taskCancelled()
		{
			_value = "Cancelled";
			synchronized(this)
			{
				_finished = true;
				notifyAll();
			}
		}

		@Override
		public void taskCompleted(String result)
		{
			_value = result;
			synchronized(this)
			{
				_finished = true;
				notifyAll();
			}
		}

		@Override
		public void taskExcepted(Exception e)
		{
			_value = e.toString();
			synchronized(this)
			{
				_finished = true;
				notifyAll();
			}
		}
	}
	
	static public void main(String []args) throws Throwable
	{
		ProgressListenerImpl impl = new ProgressListenerImpl();
		
		TestTask tt = new TestTask(1L, true, 
			"One", "Two", "Three", "Four", "Five");
		ProgressMonitor<String> monitor = new ProgressMonitor<String>();
		monitor.addProgressListener(impl, false);
		monitor.addProgressNotifier(new DefaultProgressNotifier(
			null, "Example", null, 1000L), false);
		monitor.startTask(tt);
		String result = impl.get();
		System.err.format("Got back %s\n", result);
	}
}