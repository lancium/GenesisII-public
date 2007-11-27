package org.morgan.ftp;

import java.io.PrintStream;

public interface ICommandHandler
{
	public ICommand getCommand();
	
	public void handleCommand(FTPSessionState sessionState,
		String verb, String parameters, PrintStream out) throws FTPException;
}