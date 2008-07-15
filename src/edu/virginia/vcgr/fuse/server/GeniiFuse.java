package edu.virginia.vcgr.fuse.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fuse.FuseUtils;
import edu.virginia.vcgr.fuse.fs.genii.GeniiFuseFileSystem;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.utils.SystemExec;
import fuse.Filesystem;
import fuse.FuseException;
import fuse.FuseMount;

public class GeniiFuse
{
	static private Log _logger = LogFactory.getLog(GeniiFuse.class);
	
	static public GeniiFuseConnection mountGenesisII(
		File mountPoint, String []additionalArguments, 
		ICallingContext callingContext, String sandbox, int uid,
		boolean daemon) 
		throws IOException, RemoteException, RNSException, 
			GeneralSecurityException
	{
		if (!mountPoint.exists())
			throw new FileNotFoundException(String.format(
				"Unable to locate mount point \"%s\".",
				mountPoint.getAbsolutePath()));
		
		String msg = FuseUtils.supportsFuse();
		if (msg != null)
			throw new IOException(msg);
			
		GeniiFuseFileSystem fs = new GeniiFuseFileSystem(
			callingContext, null, sandbox);
		
		GeniiFuseMount mount = new GeniiFuseMount(
			fs, uid);
		String []args = new String[additionalArguments.length + 1];
		args[0] = mountPoint.getAbsolutePath();
		System.arraycopy(additionalArguments, 0, args, 1, additionalArguments.length);
		
		GeniiMountRunner runner = new GeniiMountRunner(mount, args);
		if (daemon)
		{
			Thread th = new Thread(runner);
			th.setName("Genesis II FUSE Mount Runner");
			th.setDaemon(false);
			th.start();
		} else
			runner.run();
		
		return new GeniiFuseConnectionImpl(mountPoint);	
	}
	
	static public GeniiFuseConnection mountGenesisII(
		File mountpoint, String []additionalArguments, int uid, boolean daemon)
		throws IOException, RemoteException, RNSException, 
			GeneralSecurityException
	{
		return mountGenesisII(mountpoint, additionalArguments,
			null, null, uid, daemon);
	}
	
	static public void unmountGenesisII(
		File mountPoint) throws FuseException
	{
		new GeniiFuseConnectionImpl(mountPoint).unmount();
	}
	
	static private class GeniiFuseConnectionImpl implements GeniiFuseConnection
	{
		private File _mountPoint;
		
		public GeniiFuseConnectionImpl(File mountPoint)
		{
			_mountPoint = mountPoint;
		}
		
		@Override
		public void unmount() throws FuseException
		{
			try
			{
				StringBuilder builder = new StringBuilder();
				
				for (String str : SystemExec.executeForMultiLineOutput(
					"fusermount", "-u", _mountPoint.getAbsolutePath()))
				{
					builder.append(str);
					builder.append('\n');
				}
				
				_logger.debug(builder);
			}
			catch (IOException ioe)
			{
				throw new FuseException("Unable to unmount file system.", ioe);
			}
		}
	}
	
	static private class GeniiMountRunner implements Runnable
	{
		volatile private Exception _error = null;
		
		private String []_arguments;
		private Filesystem _fs;
		
		public GeniiMountRunner(Filesystem fs, String []arguments)
		{
			_fs = fs;
			_arguments = arguments;
		}
		
		public void run()
		{
			try
			{
				FuseMount.mount(_arguments, _fs);
			}
			catch (Exception e)
			{
				_error = e;
			}

			System.err.println("Exiting fuse mount.");
		}
		
		public Exception getError()
		{
			return _error;
		}
	}
}
