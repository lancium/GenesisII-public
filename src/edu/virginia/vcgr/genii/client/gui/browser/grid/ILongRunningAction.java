package edu.virginia.vcgr.genii.client.gui.browser.grid;

/**
 * Long running actions are actions that menu plug-ins need to perform that can
 * potentially take a long time to execute.
 * 
 * @author mmm2a
 */
public interface ILongRunningAction
{
	/**
	 * This method is called by the dialog long running job manager when
	 * the long running action is given it's turn to compute.
	 * 
	 * @param actionContext The action context that the long running
	 * action can use to make requests back to the main dialog.
	 * 
	 * @throws Throwable
	 */
	public void run(IActionContext actionContext) throws Throwable;
}