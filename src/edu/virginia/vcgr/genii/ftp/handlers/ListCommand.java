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

import java.io.OutputStream;
import java.io.PrintStream;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.ftp.FTPException;
import edu.virginia.vcgr.genii.ftp.FtpSession;
import edu.virginia.vcgr.genii.ftp.IFTPCommandHandler;
import edu.virginia.vcgr.genii.ftp.ListEntry;

public class ListCommand extends AbstractHandler
{
	static final private String _VERB = "LIST";
	
	public ListCommand(FtpSession ftpSession)
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
		
		out.println("150 Beginning Directory Listing.");
		out.flush();
		
		PrintStream writer = null;
		try
		{
			ListEntry []entries = getFileSystem().list();
			OutputStream os = pasv.getChannel().getOutputStream();
			if (os == null)
				throw new FTPException(435,
					"Data connection not established.");
			writer = new PrintStream(os);
			
			for (ListEntry entry : entries)
			{
				writer.print(entry + "\r\n");
			}
			writer.flush();
			
			out.println("226 Listing complete.");
			out.flush();
		}
		finally
		{
			StreamUtils.close(writer);
			try { pasv.close(); } catch (Throwable t) {}
		}
	}
}
