package org.morgan.ftp.cmd;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;
import org.morgan.ftp.AbstractCommandHandler;
import org.morgan.ftp.FTPAction;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FTPSessionState;
import org.morgan.ftp.ICommand;
import org.morgan.ftp.ICommandHandler;
import org.morgan.ftp.InternalException;
import org.morgan.ftp.ListEntry;
import org.morgan.ftp.RollingCommandHistory;
import org.morgan.util.io.StreamUtils;

public class NLSTCommandHandler extends AbstractCommandHandler
{
	static private Logger _logger = Logger.getLogger(NLSTCommandHandler.class);
	
	public NLSTCommandHandler(ICommand command)
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
		
		try
		{
			channel = key.getChannel(sessionState.getConfiguration().getDataConnectionTimeoutSeconds());
			
			out.println("150 Beginning Directory Listing.");
			ListEntry []entries = sessionState.getBackend().list();
			Socket sock = channel.socket();
			out2 = sock.getOutputStream();
			PrintStream writer = new PrintStream(out2);
			for (ListEntry entry : entries)
			{
				writer.print(entry.getName() + "\r\n");
			}
			writer.flush();
			
			out.println("226 Listing complete.");
		}
		catch (IOException ioe)
		{
			_logger.warn("Error trying to do directory listing.", ioe);
			throw new InternalException("Unable to do directory listing.", ioe);
		}
		finally
		{
			StreamUtils.close(out2);
			StreamUtils.close(channel);
		}
	}
}