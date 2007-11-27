package org.morgan.ftp.cmd;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;
import org.morgan.ftp.AbstractCommandHandler;
import org.morgan.ftp.FTPAction;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FTPSessionState;
import org.morgan.ftp.ICommand;
import org.morgan.ftp.ICommandHandler;
import org.morgan.ftp.InternalException;
import org.morgan.ftp.RollingCommandHistory;
import org.morgan.util.io.StreamUtils;

public class StoreCommandHandler extends AbstractCommandHandler
{
	static private final int _BUFFER_CAPACITY = 1024 * 8;
	
	static private Logger _logger = Logger.getLogger(StoreCommandHandler.class);
	
	public StoreCommandHandler(ICommand command)
	{
		super(command);
	}
	
	@Override
	public void handleCommand(FTPSessionState sessionState, String verb,
			String parameters, PrintStream out) throws FTPException
	{
		RollingCommandHistory history = sessionState.getHistory();
		FTPAction lastCompletedAction = history.lastCompleted();
		
		ICommandHandler handler = lastCompletedAction.getHandler();
		if (!(handler instanceof PASVCommandHandler))
			throw new FTPException(435, "Data connection not established.");
		
		PASVCommandHandler pHandler = (PASVCommandHandler)handler;
		DataChannelKey key = pHandler.getDataChannel();
		SocketChannel channel = null;
		OutputStream out2 = null;
		
		try
		{
			channel = key.getChannel(sessionState.getConfiguration().getDataConnectionTimeoutSeconds());
			
			out.println("150 Beginning to store file.");
			
			out2 = sessionState.getBackend().store(parameters);
			ByteBuffer buffer = ByteBuffer.allocate(_BUFFER_CAPACITY);
			
			while (channel.read(buffer) > 0)
			{
				buffer.flip();
				out2.write(buffer.array(), buffer.position(), buffer.limit());
				buffer.rewind();
			}
			out.println("226 File transferred.");
		}
		catch (IOException ioe)
		{
			_logger.warn("Error trying to store file.", ioe);
			throw new InternalException("Unable to store file.", ioe);
		}
		finally
		{
			StreamUtils.close(out2);
			StreamUtils.close(channel);
		}
	}
}