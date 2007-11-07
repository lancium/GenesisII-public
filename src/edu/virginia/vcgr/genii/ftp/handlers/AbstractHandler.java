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

import java.io.IOException;

import edu.virginia.vcgr.genii.ftp.FtpSession;
import edu.virginia.vcgr.genii.ftp.IFTPCommandHandler;
import edu.virginia.vcgr.genii.ftp.IFTPFileSystem;

public abstract class AbstractHandler implements IFTPCommandHandler
{
	protected FtpSession _ftpSession;
	private String []_handledCommands;
	
	protected AbstractHandler(FtpSession ftpSession,
		String... handledCommands)
	{
		_ftpSession = ftpSession;
		_handledCommands = handledCommands;
	}
	
	public String[] getHandledCommands()
	{
		return _handledCommands;
	}

	public void close() throws IOException
	{
	}
	
	protected IFTPFileSystem getFileSystem()
	{
		return _ftpSession.getFileSystem();
	}
	
	protected void setFileSystem(IFTPFileSystem fs)
	{
		_ftpSession.setFileSystem(fs);
	}
}
