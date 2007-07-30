/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.ftp.handlers;

import java.io.PrintStream;

import edu.virginia.vcgr.genii.ftp.FTPException;
import edu.virginia.vcgr.genii.ftp.FtpSession;
import edu.virginia.vcgr.genii.ftp.IFTPCommandHandler;
import edu.virginia.vcgr.genii.ftp.IFTPFileSystem;
import edu.virginia.vcgr.genii.ftp.IFTPListener;

public class UserCommand extends AbstractHandler
{
	static private final String _VERB = "USER";

	private String _userName = null;
	private IFTPListener []_listeners;
	private int _sessionID;
	
	public UserCommand(IFTPListener []listeners, int sessionID,
			FtpSession ftpSession)
	{
		super(ftpSession, _VERB);
		
		_listeners = listeners;
		_sessionID = sessionID;
	}
	
	public void handleCommand(IFTPCommandHandler perviousHandler, 
		String verb, String parameters, PrintStream out)
			throws FTPException
	{
		_userName = parameters;
		IFTPFileSystem fs = getFileSystem().authenticate(_userName, null);
		if (fs != null)
		{
			out.println("230 User " + _userName + " OK.");
			setFileSystem(fs);
			fireUserLoggedIn();
		} else
			out.println("331 Need a password for user " + _userName);
	}
	
	public String getUserName()
	{
		return _userName;
	}
	
	protected void fireUserLoggedIn()
	{
		for (IFTPListener listener : _listeners)
		{
			listener.userLoggedIn(_sessionID, _userName);
		}
	}
}
