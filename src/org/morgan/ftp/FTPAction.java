package org.morgan.ftp;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;

import org.morgan.util.io.StreamUtils;

public class FTPAction implements Closeable
{
	private ICommandHandler _handler;
	private Date _received;
	private Date _completed;
	
	public FTPAction(ICommandHandler handler)
	{
		_handler = handler;
		_received = new Date();
		_completed = null;
	}
	
	protected void finalize() throws Throwable
	{
		super.finalize();
		
		close();
	}
	
	public ICommandHandler getHandler()
	{
		return _handler;
	}
	
	public Date received()
	{
		return _received;
	}
	
	public void complete()
	{
		_completed = new Date();
	}
	
	/**
	 * The timestamp on which this ftp action completed.  This value CAN be null if the command
	 * hasn't completed yet.
	 * 
	 * @return THe timestamp when the command completed.
	 */
	public Date completed()
	{
		return _completed;
	}

	@Override
	synchronized public void close() throws IOException
	{
		if (_handler != null)
		{
			if (_handler instanceof Closeable)
				StreamUtils.close((Closeable)_handler);
			_handler = null;
		}
	}
}