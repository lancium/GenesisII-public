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

import org.apache.commons.logging.Log;

public class FTPException extends Exception
{
	static final long serialVersionUID = 0;
	
	private String []_ftpLines;
	
	public FTPException(String []ftpLines)
	{
		_ftpLines = ftpLines;
	}
	
	public FTPException(String line)
	{
		this(new String[] {line});
	}
	
	public FTPException(int number, String line)
	{
		this(String.format("%1$d %2$s", number, line));
	}
	
	public void communicate(PrintStream ps)
	{
		for (String line : _ftpLines)
		{
			ps.println(line);
		}
		
		ps.flush();
	}
	
	public void communicate(Log logger)
	{
		for (String line : _ftpLines)
		{
			logger.warn("FTP Exception:  " + line);
		}
	}
}
