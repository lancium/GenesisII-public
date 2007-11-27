package org.morgan.ftp;

import java.net.InetAddress;

public interface IFTPListener
{
	public void sessionCreated(int sessionID, InetAddress remoteAddress);
	public void sessionClosed(int sessionID);
	
	public void userLoggedIn(int sessionID, String username);
}