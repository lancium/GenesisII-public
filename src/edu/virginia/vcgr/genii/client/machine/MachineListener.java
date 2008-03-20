package edu.virginia.vcgr.genii.client.machine;

public interface MachineListener
{
	public void userLoggedIn(boolean loggedIn);
	public void screenSaverActivated(boolean activated);
}