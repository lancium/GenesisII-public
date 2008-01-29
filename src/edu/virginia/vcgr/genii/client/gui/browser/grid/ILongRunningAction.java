package edu.virginia.vcgr.genii.client.gui.browser.grid;

public interface ILongRunningAction
{
	public void run(IActionContext actionContext) throws Throwable;
}