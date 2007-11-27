package org.morgan.ftp;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectiveCommand extends AbstractCommand
{
	private Constructor<? extends ICommandHandler> _constructor;
	
	public ReflectiveCommand(Class<? extends ICommandHandler> handlerClass, String...handledVerbs)
		throws NoSuchMethodException
	{
		super(handledVerbs);
		
		_constructor = handlerClass.getConstructor(ICommand.class);
	}
	
	@Override
	public ICommandHandler createHandler()
		throws FTPException
	{
		try
		{
			return _constructor.newInstance(this);
		}
		catch (InstantiationException ie)
		{
			// This shouldn't happen
			throw new InternalException("Unknown internal exception.", ie);
		}
		catch (IllegalAccessException iae)
		{
			// This shouldn't happen
			throw new InternalException("Unknown internal exception.", iae);
		}
		catch (InvocationTargetException ite)
		{
			Throwable cause = ite.getCause();
			if (cause == null)
				cause = ite;
			
			throw new InternalException("Unable to create command instance.", cause);
		}
	}
}