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

import java.io.InputStream;
import java.io.PrintStream;

import edu.virginia.vcgr.genii.ftp.FTPException;
import edu.virginia.vcgr.genii.ftp.FtpSession;
import edu.virginia.vcgr.genii.ftp.IFTPCommandHandler;

public class StoreCommand extends AbstractHandler
{
	static final private String _VERB = "STOR";
	
	public StoreCommand(FtpSession ftpSession)
	{
		super(ftpSession, _VERB);
	}
	
	public void handleCommand(IFTPCommandHandler previousHandler, 
		String verb, String parameters, PrintStream out)
			throws FTPException
	{
		if (previousHandler == null ||
			!(previousHandler instanceof PASVCommand))
			throw new FTPException(435, 
				"Data connection not established.");
		
		PASVCommand pasv = (PASVCommand)previousHandler;
		
		out.println("150 Beginning to store file.");
		out.flush();

		try
		{
			InputStream in = pasv.getChannel().getInputStream();
			if (in == null)
				throw new FTPException(435,
					"Data connection not established.");
			
			getFileSystem().store(parameters, in);
			
			out.println("226 File transferred.");
			out.flush();
		}
		finally
		{
			try { pasv.close(); } catch (Throwable t) {}
		}
	}
}
