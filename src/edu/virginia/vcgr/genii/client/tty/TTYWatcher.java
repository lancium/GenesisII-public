package edu.virginia.vcgr.genii.client.tty;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;

public class TTYWatcher
{
	static private final int DEFAULT_BUFFER_SIZE = 1024 * 8;
	
	static private WatcherThread _watcherThread = null;
	
	synchronized static public void watch(PrintWriter stdout, PrintWriter stderr,
		EndpointReferenceType tty) throws TTYException, RemoteException, 
			FileNotFoundException, IOException
	{
		if (_watcherThread != null)
			throw new TTYException("Already watching a tty object.");
		
		_watcherThread = new WatcherThread(stdout, stderr, tty);
		_watcherThread.start();
	}
	
	synchronized static public void unwatch()
		throws TTYException
	{
		if (_watcherThread == null)
			throw new TTYException("Not currently watching a tty object.");
		
		_watcherThread.close();
		_watcherThread = null;
	}
	
	static private class WatcherThread extends Thread implements Closeable
	{
		private PrintWriter _out;
		private PrintWriter _err;
		private InputStream _in;
		private boolean _quit;
		
		public WatcherThread(PrintWriter out, PrintWriter err,
			EndpointReferenceType epr)
				throws RemoteException, IOException,
					FileNotFoundException
		{
			super("TTY Watcher Thread");
			
			setDaemon(false);
			_out = out;
			_err = err;
			_in = ByteIOStreamFactory.createInputStream(epr);
			_quit = false;
		}
		
		protected void finalize() throws Throwable
		{
			super.finalize();
			
			close();
		}
		
		synchronized public void close()
		{
			if (_in != null)
			{
				_quit = true;
				interrupt();
				StreamUtils.close(_in);
				_in = null;
			}
		}
		
		public void run()
		{
			byte []data = new byte[DEFAULT_BUFFER_SIZE];
			while (!_quit)
			{
				try
				{
					int read = _in.read(data);
					if (read < 0)
						return;
					_out.write(new String(data, 0, read));
					_out.flush();
				}
				catch (Throwable cause)
				{
					if (_quit)
						return;
					
					_err.format("Error in TTY Watcher (%s)-- Quiting\n", 
						cause.getLocalizedMessage());
					_quit = true;
				}
			}
		}
	}
}