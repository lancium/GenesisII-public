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
package edu.virginia.vcgr.genii.ftp;

import java.io.PrintStream;
import java.net.InetAddress;

public class FTPPrintStreamListener implements IFTPListener
{
	private PrintStream _out;
	
	public FTPPrintStreamListener(PrintStream out)
	{
		_out = out;
	}
	
	public void sessionCreated(int sessionID, InetAddress remoteAddress)
	{
		_out.println("[" + sessionID + "] Connection from " +
			remoteAddress + ".");
		_out.flush();
	}

	public void sessionClosed(int sessionID)
	{
		_out.println("[" + sessionID + "] Connection closed.");
		_out.flush();
	}

	public void userLoggedIn(int sessionID, String username)
	{
		_out.println("[" + sessionID + "] User \"" + username 
			+ "\" authenticated.");
		_out.flush();
	}
}
