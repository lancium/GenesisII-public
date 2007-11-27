package org.morgan.ftp.cmd;

import java.io.PrintStream;

import org.morgan.ftp.AbstractCommandHandler;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FTPSessionState;
import org.morgan.ftp.ICommand;

public class MkdirCommandHandler extends AbstractCommandHandler
{
	public MkdirCommandHandler(ICommand parent)
	{
		super(parent);
	}
	
	@Override
	public void handleCommand(FTPSessionState sessionState, String verb,
			String parameters, PrintStream out) throws FTPException
	{
		String result = sessionState.getBackend().mkdir(parameters);
		out.println("257 \"" + result + "\"");
	}
}