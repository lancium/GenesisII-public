package org.morgan.ftp.cmd;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;

import org.apache.log4j.Logger;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.Hostname;

public class DataChannelManager
{
	static private Logger _logger = Logger.getLogger(DataChannelManager.class);
	
	static private String describeSocket(ServerSocket ss) throws SocketException
	{
		try
		{
			int port = ss.getLocalPort();
			
			_logger.info("Describing socket port " + port);
			return String.format("=%s,%d,%d",
					Hostname.getLocalHostname().getAddress().getHostAddress().replace(
					'.', ','), port/256, port%256);
		}
		catch (UnknownHostException uhe)
		{
			throw new SocketException("Unknown host.");
		}
	}
	
	static public DataChannelKey acquireDataChannel(long timeoutSeconds)
		throws IOException, SocketException
	{
		String socketDescription;
		ServerSocketChannel serverChannel = null;
		ServerSocket servSock;
		
		try
		{
			serverChannel = ServerSocketChannel.open();
			servSock = serverChannel.socket();
			servSock.bind(null, 5);
			
			socketDescription = describeSocket(servSock);
			
			Date timeoutDate = new Date(System.currentTimeMillis() + timeoutSeconds * 1000);
			DataChannelImpl ret = new DataChannelImpl(socketDescription);
			Thread th = new Thread(new ChannelHandler(serverChannel, ret, timeoutDate));
			th.setName("Data Channel Manager Thread");
			th.setDaemon(true);
			th.start();
			serverChannel = null;
			
			return ret;
		}
		finally
		{
			StreamUtils.close(serverChannel);
		}
	}
	
	static private class ChannelHandler implements Runnable
	{
		private Date _timeoutDate;
		private DataChannelImpl _dataChannel;
		private ServerSocketChannel _serverChannel;
		
		public ChannelHandler(ServerSocketChannel serverChannel, DataChannelImpl dataChannel, Date timeoutDate)
		{
			_timeoutDate = timeoutDate;
			_dataChannel = dataChannel;
			_serverChannel = serverChannel;
		}
		
		@Override
		public void run()
		{
			SocketAcceptor acceptor = null;
			
			try
			{
				acceptor = new SocketAcceptor(_serverChannel);
				SocketChannel channel = acceptor.accept(_timeoutDate);
				_dataChannel.manageSocketChannel(channel, _timeoutDate);
			} 
			catch (IOException ioe)
			{
				_logger.error("IO Exception while trying to create data channel.", ioe);
			}
			finally
			{
				StreamUtils.close(acceptor);
			}
		}
	}
	
	static private class SocketAcceptor implements Closeable
	{
		private ServerSocketChannel _serverChannel;
		
		public SocketAcceptor(ServerSocketChannel serverChannel)
		{
			_serverChannel = serverChannel;
		}
		
		protected void finalize() throws Throwable
		{
			close();
		}
		
		public SocketChannel accept(Date timeoutDate)
			throws IOException
		{
			_serverChannel.configureBlocking(false);
			Selector selector = null;
			SelectionKey key = null;
			
			try
			{
				selector = Selector.open();
				key = _serverChannel.register(selector, SelectionKey.OP_ACCEPT);
				
				while (true)
				{
					Date now = new Date();
					if (now.after(timeoutDate))
						return null;
					
					long timeout = timeoutDate.getTime() - now.getTime();
					if (timeout <= 0)
						timeout = 1;
					
					if (selector.select(timeout) == 1)
					{
						SocketChannel s = _serverChannel.accept();
						s.configureBlocking(true);
						return s;
					}
				}
			}
			finally
			{
				if (key != null)
					key.cancel();
				
				selector.close();
			}
		}
		
		public void close()
		{
			if (_serverChannel != null)
				try { _serverChannel.close(); } catch (Throwable cause) {}
		}
	}
	
	static private class DataChannelImpl implements DataChannelKey
	{
		volatile private boolean _cleanedUp = false;
		private SocketChannel _channel;
		private String _socketDescription;
		private Object _signalObj = new Object();
		private boolean _gaveUp = false;
		
		public DataChannelImpl(String socketDescription)
		{
			_socketDescription = socketDescription;
			_channel = null;
		}
		
		protected void finalize() throws Throwable
		{
			close();
		}
		
		public void manageSocketChannel(SocketChannel channel, Date timeoutDate)
		{
			synchronized(_signalObj)
			{
				_channel = channel;
				
				if (!_gaveUp)
				{
					if (channel == null)
					{
						_gaveUp = true;
						_signalObj.notifyAll();
						return;
					}
					
					while (!_cleanedUp)
					{
						_signalObj.notifyAll();
						
						Date now = new Date();
						if (now.after(timeoutDate))
							break;
						
						long timeout = timeoutDate.getTime() - now.getTime();
						if (timeout <= 0)
							timeout = 1;
						
						try { _signalObj.wait(timeout); } catch (InterruptedException ie) {}
					}
				}
				
				if (!_cleanedUp)
				{
					_logger.warn("Timing out unclaimed socket channel.");
					try { _channel.close(); } catch (Throwable cause) {}
				}
			}
		}
		
		@Override
		public SocketChannel getChannel(long timeoutSeconds)
		{
			Date timeoutDate = new Date(System.currentTimeMillis() + timeoutSeconds * 1000);
			synchronized(_signalObj)
			{
				while (true)
				{
					if (_gaveUp)
						return null;
					
					if (_channel != null && !_cleanedUp)
					{
						_signalObj.notifyAll();
						SocketChannel channel = _channel;
						_channel = null;
						_cleanedUp = true;
						return channel;
					}
					
					Date now = new Date();
					if (now.after(timeoutDate))
					{
						_logger.warn("Command timing out waiting for connected channel.");
						_gaveUp = true;
						return null;
					}
					
					long timeout = timeoutDate.getTime() - now.getTime();
					if (timeout <= 0)
						timeout = 1;
					
					try { _signalObj.wait(timeout); } catch (InterruptedException ie) {}
				}
			}
		}

		@Override
		public String getServerSocketDescription()
		{
			return _socketDescription;
		}
		
		public void close()
		{
			synchronized(_signalObj)
			{
				_gaveUp = true;
				_signalObj.notifyAll();
				
				if (!_cleanedUp)
				{
					_cleanedUp = true;
					StreamUtils.close(_channel);
					_channel = null;
				}
			}
		}
	}
}