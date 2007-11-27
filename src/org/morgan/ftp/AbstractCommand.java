package org.morgan.ftp;

public abstract class AbstractCommand implements ICommand
{
	private String[] _handledVerbs;
	
	protected AbstractCommand(String []handledVerbs)
	{
		_handledVerbs = handledVerbs;
	}
	
	@Override
	public String[] getHandledVerbs()
	{
		return _handledVerbs;
	}
}