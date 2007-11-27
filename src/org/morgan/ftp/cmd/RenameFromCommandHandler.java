package org.morgan.ftp.cmd;

import java.io.PrintStream;

import org.morgan.ftp.AbstractCommandHandler;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FTPSessionState;
import org.morgan.ftp.ICommand;
import org.morgan.ftp.PathDoesNotExistException;

public class RenameFromCommandHandler extends AbstractCommandHandler
{
	private String _renameFrom;
	
	public RenameFromCommandHandler(ICommand command)
	{
		super(command);
	}
	
	@Override
	public void handleCommand(FTPSessionState sessionState, String verb,
			String parameters, PrintStream out) throws FTPException
	{
		_renameFrom = parameters;
		
		if (sessionState.getBackend().exists(parameters))
			out.println("350 OK");
		else
			throw new PathDoesNotExistException(parameters);
	}
	
	public String getFrom()
	{
		return _renameFrom;
	}
}