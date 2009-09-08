package edu.virginia.vcgr.genii.ui;

public interface QuitListener
{
	/**
	 * Called when the application is about to exit.
	 * 
	 * @return True if the quit was accepted, false if we can't quit yet.
	 */
	public boolean quitRequested();
}