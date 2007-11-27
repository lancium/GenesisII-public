package org.morgan.ftp.cmd;

import java.io.PrintStream;

import org.morgan.ftp.AbstractCommandHandler;
import org.morgan.ftp.FTPAction;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FTPSessionState;
import org.morgan.ftp.ICommand;

public class RenameToCommandHandler extends AbstractCommandHandler
{
	public RenameToCommandHandler(ICommand command)
	{
		super(command);
	}
	
	@Override
	public void handleCommand(FTPSessionState sessionState, String verb,
			String parameters, PrintStream out) throws FTPException
	{
		FTPAction action = sessionState.getHistory().lastCompleted();
		if (action == null || !(action.getHandler() instanceof RenameFromCommandHandler))
			out.println("503 Must have RNFR first.");
		
		RenameFromCommandHandler handler = (RenameFromCommandHandler)action.getHandler();
		sessionState.getBackend().rename(handler.getFrom(), parameters);
		out.println("250 OK");
	}
}