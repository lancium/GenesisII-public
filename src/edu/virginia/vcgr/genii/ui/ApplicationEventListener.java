package edu.virginia.vcgr.genii.ui;

public interface ApplicationEventListener
{
	public void preferencesRequested();
	public void aboutRequested();
	public boolean quitRequested();
}
