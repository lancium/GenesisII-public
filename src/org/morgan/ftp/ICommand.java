package org.morgan.ftp;

public interface ICommand
{
	public String[] getHandledVerbs();
	
	public ICommandHandler createHandler() throws FTPException;
}