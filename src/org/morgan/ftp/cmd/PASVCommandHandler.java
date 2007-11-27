package org.morgan.ftp.cmd;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.morgan.ftp.AbstractCommandHandler;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FTPSessionState;
import org.morgan.ftp.ICommand;
import org.morgan.ftp.InternalException;
import org.morgan.util.io.StreamUtils;

public class PASVCommandHandler extends AbstractCommandHandler implements Closeable
{
	static private Logger _logger = Logger.getLogger(PASVCommandHandler.class);
	
	private DataChannelKey _dataChannel = null;
	
	public PASVCommandHandler(ICommand command)
	{
		super(command);
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	@Override
	public void handleCommand(FTPSessionState sessionState, String verb,
			String parameters, PrintStream out) throws FTPException
	{
		try
		{
			_dataChannel = DataChannelManager.acquireDataChannel(
				sessionState.getConfiguration().getDataConnectionTimeoutSeconds());
			
			out.println("227 " + _dataChannel.getServerSocketDescription());
		}
		catch (IOException ioe)
		{
			_logger.error("Unable to create data channel.", ioe);
			throw new InternalException("Unable to create data channel.", ioe);
		}
	}
	
	synchronized public DataChannelKey getDataChannel()
	{
		return _dataChannel;
	}
	
	synchronized public void close()
	{
		StreamUtils.close(_dataChannel);
		_dataChannel = null;
	}
}