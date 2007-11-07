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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.ftp.handlers.CWDCommand;
import edu.virginia.vcgr.genii.ftp.handlers.DeleteCommand;
import edu.virginia.vcgr.genii.ftp.handlers.ListCommand;
import edu.virginia.vcgr.genii.ftp.handlers.MkdirCommand;
import edu.virginia.vcgr.genii.ftp.handlers.PASVCommand;
import edu.virginia.vcgr.genii.ftp.handlers.PWDCommand;
import edu.virginia.vcgr.genii.ftp.handlers.PasswordCommand;
import edu.virginia.vcgr.genii.ftp.handlers.RenameFromCommand;
import edu.virginia.vcgr.genii.ftp.handlers.RenameToCommand;
import edu.virginia.vcgr.genii.ftp.handlers.RetrieveCommand;
import edu.virginia.vcgr.genii.ftp.handlers.RmdirCommand;
import edu.virginia.vcgr.genii.ftp.handlers.StoreCommand;
import edu.virginia.vcgr.genii.ftp.handlers.TypeCommand;
import edu.virginia.vcgr.genii.ftp.handlers.UserCommand;

public class FtpSession extends Thread implements Closeable, IdleReapable
{
	private static Log _logger = LogFactory.getLog(FtpSession.class);
	
	private int _authAttemptCount = 0;
	private FTPConfiguration _configuration;
	private int _sessionID;
	private IFTPListener []_listeners;
	
	private IFTPFileSystem _fileSystem;
	private Socket _socket;
	private HashMap<String, IFTPCommandHandler> _commands;
	private IFTPCommandHandler _lastCommand = null;
	private String _greeting;
	
	private Object _reapSync = new Object();
	private Date _lastReadlineStarted;
	
	private void addCommandHandler(IFTPCommandHandler handler)
	{
		String []verbs = handler.getHandledCommands();
		
		for (String verb : verbs)
		{
			_commands.put(verb, handler);
		}
	}
	
	protected void finalize() throws Throwable
	{
		try
		{
			close();
		}
		finally
		{
			super.finalize();
		}
	}
	
	FtpSession(FTPConfiguration configuration,
		IFTPListener []listeners, int sessionID, 
		ICallingContext ctxt, Socket socket)
	{
		_configuration = configuration;
		_listeners = listeners;
		_sessionID = sessionID;
		
		_socket = socket;
		
		_fileSystem = new UnauthenticatedVCGRFileSystem(ctxt);
		_commands = new HashMap<String, IFTPCommandHandler>();
		
		_greeting = _fileSystem.getGreeting();
		
		addCommandHandler(new UserCommand(_listeners, _sessionID,
			this));
		addCommandHandler(new PasswordCommand(_listeners, _sessionID,
				this));
		addCommandHandler(new PWDCommand(this));
		addCommandHandler(new TypeCommand(this));
		addCommandHandler(new CWDCommand(this));
//		addCommandHandler(new SizeCommand(this));
		addCommandHandler(new PASVCommand(this));
		addCommandHandler(new ListCommand(this));
		addCommandHandler(new RetrieveCommand(this));
		addCommandHandler(new StoreCommand(this));
		addCommandHandler(new RmdirCommand(this));
		addCommandHandler(new DeleteCommand(this));
		addCommandHandler(new RenameFromCommand(this));
		addCommandHandler(new RenameToCommand(this));
		addCommandHandler(new MkdirCommand(this));
	}
	
	public int getSessionID()
	{
		return _sessionID;
	}
	
	public void run()
	{
		IFTPCommandHandler handler = null;
		IFTPCommandHandler _lastHandler = null;
		
		BufferedReader reader = null;
		PrintStream out = null;
		
		String line;
		Pattern verbExtractor = Pattern.compile(
			"\\s*(\\w+)\\s*(.*)\\s*");
		
		try
		{
			reader = new BufferedReader(new InputStreamReader(
				_socket.getInputStream()));
			out = new PrintStream(_socket.getOutputStream());
			
			out.println("220 " + _greeting);
			_logger.info("220 " + _greeting);
			out.flush();
			
			startReadline();
			while ( (line = reader.readLine()) != null)
			{
				stopReadline();
				_logger.debug("FTP Session Received \"" + line + "\".");
				Matcher matcher = verbExtractor.matcher(line);
				if (!matcher.matches())
				{
					_logger.error("FTP command unrecognized.");
					continue;
				}
				
				String verb = matcher.group(1);
				String parameters = matcher.group(2);
				
				handler = _commands.get(verb);
				
				try
				{
					if (handler == null)
						throw new UnimplementedException(verb);
					handler.handleCommand(_lastCommand, verb, parameters, 
						out);
					out.flush();
					
					if (_lastCommand != null)
						try { _lastCommand.close(); } 
							catch (IOException ioe) {}
						
					if (_lastCommand != null)
						_lastCommand.close();
					
					_lastCommand = handler;
				}
				catch (FTPException ftpe)
				{
					ftpe.communicate(_logger);
					ftpe.communicate(out);
				}
				
				startReadline();
			}
		}
		catch (IOException ioe)
		{
			_logger.warn("Unknown IO Exception in FTP Session.", ioe);
		}
		catch (Throwable cause)
		{
			_logger.error("Unknown error occurred in FTP Session.", cause);
		}
		finally
		{
			StreamUtils.close(_lastHandler);
			StreamUtils.close(handler);
			
			_logger.info("Closing FTP Session.");
			
			StreamUtils.close(out);
			StreamUtils.close(reader);
			
			close();
		}
	}
	
	public void close()
	{
		boolean sendClose = false;
		
		synchronized (this)
		{
			if (_socket != null)
			{
				try { _socket.shutdownOutput(); } catch (Throwable t) {}
				try { _socket.shutdownInput(); } catch (Throwable t) {}
				try { _socket.close(); } catch (Throwable t) {}
				
				sendClose = true;
			}
			
			_socket = null;
		}
		
		if (sendClose)
		{
			for (IFTPListener listener : _listeners)
			{
				listener.sessionClosed(_sessionID);
			}
		}
	}
	
	public IFTPFileSystem getFileSystem()
	{
		return _fileSystem;
	}
	
	public void setFileSystem(IFTPFileSystem fs)
	{
		_fileSystem = fs;
	}
	
	public FTPConfiguration getConfiguration()
	{
		return _configuration;
	}
	
	public void incrementAuthAttempt() throws UnauthenticatedException
	{
		_authAttemptCount++;
		if (_configuration.getMissedAuthentiationsLimit() 
			<= _authAttemptCount)
			throw new UnauthenticatedException();
	}

	private void startReadline()
	{
		synchronized(_reapSync)
		{
			_lastReadlineStarted = new Date();
		}
	}
	
	private void stopReadline()
	{
		synchronized(_reapSync)
		{
			_lastReadlineStarted = null;
		}
	}
	
	public boolean reapable()
	{
		long diff = -1;
		
		synchronized(_reapSync)
		{
			if (_lastReadlineStarted != null)
			{
				Date now = new Date();
				diff = now.getTime() - _lastReadlineStarted.getTime();
			}
		}
		
		long timeout = _configuration.getIdleTimeoutSeconds() * 1000L;
		if (diff >= timeout)
			return true;
		
		return false;
	}

	public void reap()
	{
		close();
	}
	
	public boolean closed()
	{
		synchronized(this)
		{
			return _socket == null;
		}
	}
}
