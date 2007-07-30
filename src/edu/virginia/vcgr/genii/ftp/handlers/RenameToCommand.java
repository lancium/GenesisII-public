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

public class RenameToCommand extends AbstractHandler
{
	static private final String _VERB = "RNTO";

	public RenameToCommand(FtpSession ftpSession)
	{
		super(ftpSession, _VERB);
	}

	public void handleCommand(IFTPCommandHandler previousHandler, 
		String verb, String parameters, PrintStream out)
			throws FTPException
	{
		if (previousHandler == null || 
			!(previousHandler instanceof RenameFromCommand))
			out.println("503 Must have RNFR first.");
		else
		{
			getFileSystem().rename(
				((RenameFromCommand)previousHandler).getFrom(),
				parameters);
			out.println("250 OK");
		}
	}
}
