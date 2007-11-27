package org.morgan.ftp.cmd;

import java.io.PrintStream;

import org.morgan.ftp.AbstractCommandHandler;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FTPSessionState;
import org.morgan.ftp.IBackend;
import org.morgan.ftp.ICommand;

public class UserCommandHandler extends AbstractCommandHandler
{
	private String _username;
	
	public UserCommandHandler(ICommand command)
	{
		super(command);
	}
	
	@Override
	public void handleCommand(FTPSessionState sessionState, String verb,
			String parameters, PrintStream out) throws FTPException
	{
		_username = parameters;
		IBackend backend = sessionState.getBackend();
		if (backend.authenticate(_username, null))
		{
			out.println("230 User " + _username + " OK.");
			sessionState.getListenerManager().fireUserAuthenticated(sessionState.getSessionID(), _username);
		} else
		{
			out.println("331 Need a password for user " + _username);
		}
	}
	
	public String getUserName()
	{
		return _username;
	}
}