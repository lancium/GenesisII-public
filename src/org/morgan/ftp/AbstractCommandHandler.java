package org.morgan.ftp;

public abstract class AbstractCommandHandler implements ICommandHandler
{
	private ICommand _command;
	
	protected AbstractCommandHandler(ICommand command)
	{
		_command = command;
	}
	
	@Override
	public ICommand getCommand()
	{
		return _command;
	}
}