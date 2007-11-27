package org.morgan.ftp;

public interface FTPListener
{
	public void sessionOpened(int sessionID);
	public void userAuthenticated(int sessionID, String username);
	public void sessionClosed(int sessionID);
}