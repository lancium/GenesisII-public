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
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.ftp.DataChannel;
import edu.virginia.vcgr.genii.ftp.FTPException;
import edu.virginia.vcgr.genii.ftp.FtpSession;
import edu.virginia.vcgr.genii.ftp.IFTPCommandHandler;
import edu.virginia.vcgr.genii.ftp.InternalException;

public class PASVCommand extends AbstractHandler
{
	static private Log _logger = LogFactory.getLog(PASVCommand.class);
	
	static final private String _VERB = "PASV";
	
	private DataChannel _channel;
	
	public PASVCommand(FtpSession ftpSession)
	{
		super(ftpSession, _VERB);
		
		_channel = null;
	}
	
	public void handleCommand(IFTPCommandHandler previousHandler, 
		String verb, String parameters, PrintStream out)
			throws FTPException
	{
		try
		{
			if (_channel == null)
			{
				_channel = new DataChannel(
					_ftpSession.getConfiguration(
						).getDataConnectionTimeoutSeconds());
				_channel.start();
			}
			
			out.println("227 " + _channel.ftpDescribe());
		}
		catch (Throwable t)
		{
			_logger.error("Couldn't create data socket for client.",
				t);
			throw new InternalException(
				"Couldn't create data socket for client.", t);
		}
	}
	
	public DataChannel getChannel()
	{
		return _channel;
	}
	
	synchronized public void close() throws IOException
	{
		if (_channel != null)
		{
			_channel.close();
			_channel = null;
		}
	}
}
