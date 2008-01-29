package edu.virginia.vcgr.genii.client.gui.browser;

import edu.virginia.vcgr.genii.client.gui.browser.grid.IActionContext;
import edu.virginia.vcgr.genii.client.gui.browser.grid.ILongRunningAction;

public class LongActionRunner implements Runnable
{
	private ILongRunningAction _action;
	private IActionContext _context;
	
	public LongActionRunner(ILongRunningAction action, IActionContext context)
	{
		_action = action;
		_context = context;
	}
	
	@Override
	public void run()
	{
		try
		{
			_action.run(_context);
		}
		catch (Throwable cause)
		{
			_context.reportError("Uncaught Exception", cause);
		}
	}
}