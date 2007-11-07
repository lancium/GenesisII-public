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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.Hostname;

public class DataChannel extends Thread implements Closeable
{
	private static Log _logger = LogFactory.getLog(DataChannel.class);
	
	private ServerSocket _sSock;
	private int _timeoutSeconds;
	private Socket _socket = null;
	private InputStream _in = null;
	private OutputStream _out = null;
	private boolean _closing = false;
	
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
	
	public DataChannel(int timeoutSeconds) throws IOException
	{
		_sSock = new ServerSocket(0);
		_sSock.setSoTimeout(1000);
		_timeoutSeconds = timeoutSeconds;
	}
	
	public void run()
	{
		try
		{
			Socket s = null;
			
			while (true)
			{
				try
				{
					s = _sSock.accept();
					if (isInterrupted())
						throw new InterruptedException();
					break;
				}
				catch (SocketTimeoutException ste)
				{
					_timeoutSeconds--;
					if (_timeoutSeconds <= 0)
						throw ste;
				}
				
				if (isInterrupted())
					throw new InterruptedException();
			}
			
			_socket = s;
			_in = s.getInputStream();
			_out = s.getOutputStream();
		}
		catch (SocketTimeoutException ste)
		{
			_logger.warn("Timeout waiting for FTP client connect.");
		}
		catch (IOException ioe)
		{
			_logger.error("IO Exception handling FTP data connect.",
				ioe);
		}
		catch (InterruptedException ie)
		{
			isInterrupted();
			_logger.debug("Interrupted the FTP Data Channel.");
		}
		finally
		{
			if (_sSock != null)
				try { _sSock.close(); } catch (Throwable t) {}
		}
	}

	public void close() throws IOException
	{
		synchronized(this)
		{
			if (_closing)
				return;
			_closing = true;
		}
		
		interrupt();
		try { join(); } catch (InterruptedException ie) {}
		
		StreamUtils.close(_out);
		StreamUtils.close(_in);
		
		try
		{
			if (_socket != null)
			{
				_socket.shutdownOutput();
				_socket.shutdownInput();
				_socket.close();
			}
		}
		finally
		{
			_socket = null;
		}
	}
	
	public InputStream getInputStream()
	{
		try
		{
			join();
		}
		catch (InterruptedException ie)
		{
			return null;
		}
		
		if (_socket == null)
			return null;
		
		return _in;
	}
	
	public OutputStream getOutputStream()
	{
		try
		{
			join();
		}
		catch (InterruptedException ie)
		{
			return null;
		}
		
		if (_socket == null)
			return null;
		
		return _out;
	}
	
	public String ftpDescribe() 
		throws UnknownHostException
	{
		int port = _sSock.getLocalPort();
		
		String response = String.format("=%1$s,%2$d,%3$d",
			Hostname.getLocalHostname().getAddress().getHostAddress().replace(
				'.', ','), port >> 8, port & 0xFF);
		
		_logger.debug("Sending responze \"" + response + "\" for port " + port);
		return response;
	}
}
