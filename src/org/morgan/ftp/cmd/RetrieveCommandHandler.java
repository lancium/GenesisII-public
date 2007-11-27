package org.morgan.ftp.cmd;

import java.io.IOException;
import java.io.InputStream;
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

public class RetrieveCommandHandler extends AbstractCommandHandler
{
	static private Logger _logger = Logger.getLogger(RetrieveCommandHandler.class);
	static private final int _BUFFER_CAPACITY = 1024 * 8;
	
	public RetrieveCommandHandler(ICommand command)
	{
		super(command);
	}
	
	@Override
	public void handleCommand(FTPSessionState sessionState, String verb,
			String parameters, PrintStream out) throws FTPException
	{
		RollingCommandHistory history = sessionState.getHistory();
		FTPAction lastCompletedAction = history.lastCompleted(PASVCommandHandler.class);
		
		ICommandHandler handler = lastCompletedAction.getHandler();
		if (!(handler instanceof PASVCommandHandler))
			throw new FTPException(435, "Data connection not established.");
		
		PASVCommandHandler pHandler = (PASVCommandHandler)handler;
		DataChannelKey key = pHandler.getDataChannel();
		SocketChannel channel = null;
		OutputStream out2 = null;
		InputStream in = null;
		
		try
		{
			channel = key.getChannel(sessionState.getConfiguration().getDataConnectionTimeoutSeconds());
			
			out.println("150 Beginning to retrieve file.");
			
			in = sessionState.getBackend().retrieve(parameters);
			ByteBuffer buffer = ByteBuffer.allocate(_BUFFER_CAPACITY);
			int bytesRead;
			while ( (bytesRead = in.read(buffer.array())) >= 0)
			{
				buffer.position(bytesRead);
				buffer.flip();
				while (buffer.hasRemaining())
					channel.write(buffer);
				buffer.rewind();
			}
			
			out.println("226 File transferred.");
		}
		catch (IOException ioe)
		{
			_logger.warn("Error trying to retrieve file.", ioe);
			throw new InternalException("Unable to retrieve file.", ioe);
		}
		finally
		{
			StreamUtils.close(in);
			StreamUtils.close(out2);
			StreamUtils.close(channel);
		}
	}
}