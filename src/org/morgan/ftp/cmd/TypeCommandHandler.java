package org.morgan.ftp.cmd;

import java.io.PrintStream;

import org.morgan.ftp.AbstractCommandHandler;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FTPSessionState;
import org.morgan.ftp.ICommand;
import org.morgan.ftp.UnimplementedException;

public class TypeCommandHandler extends AbstractCommandHandler
{
	public TypeCommandHandler(ICommand command)
	{
		super(command);
	}
	
	@Override
	public void handleCommand(FTPSessionState sessionState, String verb,
			String parameters, PrintStream out) throws FTPException
	{
		if (parameters.equals("A") || parameters.equals("A N"))
			out.println("200 OK");
		else if (parameters.equals("I") || parameters.equals("L 8"))
			out.println("200 OK");
		else
			throw new UnimplementedException("TYPE", parameters);
	}
}