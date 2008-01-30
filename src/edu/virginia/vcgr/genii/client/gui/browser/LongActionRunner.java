package edu.virginia.vcgr.genii.client.gui.browser;

import edu.virginia.vcgr.genii.client.gui.browser.grid.IActionContext;
import edu.virginia.vcgr.genii.client.gui.browser.grid.ILongRunningAction;

/**
 * The long action runner is the wrapper class that wraps a long
 * running action and handles it inside of a seperate thread.
 * 
 * @author mmm2a
 */
class LongActionRunner implements Runnable
{
	private ILongRunningAction _action;
	private IActionContext _context;
	
	/**
	 * Create a new long action runner.
	 * 
	 * @param action The long running action to handle.
	 * @param context The action context to use for running the job.
	 */
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