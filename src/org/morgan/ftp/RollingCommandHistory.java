package org.morgan.ftp;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.morgan.util.io.StreamUtils;

public class RollingCommandHistory implements Iterable<FTPAction>, Closeable
{
	private LinkedList<FTPAction> _history = new LinkedList<FTPAction>();
	private int _historyCapacity;
	
	public RollingCommandHistory(int historyCapacity)
	{
		_historyCapacity = historyCapacity;
	}
	
	protected void finalize() throws Throwable
	{
		super.finalize();
		
		close();
	}
	
	public FTPAction addCommand(ICommandHandler handler)
	{
		FTPAction ret;
		_history.addFirst(ret = new FTPAction(handler));
		while (_history.size() > _historyCapacity)
		{
			StreamUtils.close(_history.removeLast());
		}
		
		return ret;
	}

	@Override
	public Iterator<FTPAction> iterator()
	{
		return _history.iterator();
	}
	
	public FTPAction lastCommand()
	{
		if (_history.size() <= 0)
			return null;
		
		return _history.getFirst();
	}
	
	public FTPAction lastCompleted()
	{
		for (FTPAction action : _history)
		{
			if (action.completed() != null)
				return action;
		}
		
		return null;
	}

	@Override
	synchronized public void close() throws IOException
	{
		FTPAction action;
		while (true)
		{
			if (_history.isEmpty())
				return;
			
			action = _history.removeLast();
			StreamUtils.close(action);
		}
	}
}