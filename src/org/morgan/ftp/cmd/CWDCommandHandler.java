package org.morgan.ftp.cmd;

import java.io.PrintStream;

import org.morgan.ftp.AbstractCommandHandler;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FTPSessionState;
import org.morgan.ftp.IBackend;
import org.morgan.ftp.ICommand;

public class CWDCommandHandler extends AbstractCommandHandler
{
	public CWDCommandHandler(ICommand command)
	{
		super(command);
	}
	
	@Override
	public void handleCommand(FTPSessionState sessionState, String verb,
			String parameters, PrintStream out) throws FTPException
	{
		IBackend backend = sessionState.getBackend();
		backend.cwd(parameters);
		out.println("250 OK");
	}
}