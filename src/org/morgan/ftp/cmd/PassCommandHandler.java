package org.morgan.ftp.cmd;

import java.io.PrintStream;

import org.morgan.ftp.AbstractCommandHandler;
import org.morgan.ftp.AuthorizationFailedException;
import org.morgan.ftp.FTPAction;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FTPSessionState;
import org.morgan.ftp.IBackend;
import org.morgan.ftp.ICommand;
import org.morgan.ftp.ICommandHandler;

public class PassCommandHandler extends AbstractCommandHandler
{
	public PassCommandHandler(ICommand command)
	{
		super(command);
	}
	
	@Override
	public void handleCommand(FTPSessionState sessionState, String verb,
			String parameters, PrintStream out) throws FTPException
	{
		FTPAction action = sessionState.getHistory().lastCompleted(null);
		if (action != null)
		{
			ICommandHandler handler = action.getHandler();
			if (handler instanceof UserCommandHandler)
			{
				IBackend backend = sessionState.getBackend();
				String username = ((UserCommandHandler)handler).getUserName();
				if (backend.authenticate(username, parameters))
				{
					out.println("230 Authenticated");
					sessionState.getListenerManager().fireUserAuthenticated(sessionState.getSessionID(), username);
					return;
				} else
				{
					throw new AuthorizationFailedException();
				}
			}
		}
		
		out.println("503 No user given.");
	}
}