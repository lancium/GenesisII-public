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

public class PasswordCommand extends AbstractHandler
{
	static final private String _VERB = "PASS";
	
	private IFTPListener []_listeners;
	private int _sessionID;
	
	public PasswordCommand(IFTPListener []listeners,
		int sessionID, FtpSession ftpSession)
	{
		super(ftpSession, _VERB);
		
		_listeners = listeners;
		_sessionID = sessionID;
	}
	
	public void handleCommand(IFTPCommandHandler previousHandler, 
		String verb, String parameters, PrintStream out)
			throws FTPException
	{
		if (previousHandler == null || 
			!(previousHandler instanceof UserCommand))
			out.println("503 No user given.");
		else
		{
			IFTPFileSystem fs =getFileSystem().authenticate(
				((UserCommand)previousHandler).getUserName(),
				parameters);
			if (fs != null)
			{
				out.println("230 Authenticated");
				setFileSystem(fs);
				fireUserLoggedIn(
					((UserCommand)previousHandler).getUserName());
			} else
			{
				out.println("530 Couldn't authenticate user.");
				_ftpSession.incrementAuthAttempt();
			}
		}
	}
	
	protected void fireUserLoggedIn(String username)
	{
		for (IFTPListener listener : _listeners)
		{
			listener.userLoggedIn(_sessionID, username);
		}
	}
}
