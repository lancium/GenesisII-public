package edu.virginia.vcgr.ogrsh.server.gtool;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.ITool;

public class GridRunManager implements Closeable
{
	static final private int STDIN = 0;
	static final private int STDOUT = 1;
	static final private int STDERR = 2;
	
	private BufferedReader _in = null;
	private PrintWriter _out = null;
	private PrintWriter _err = null;
	
	private ITool _gridTool;
	
	static private void readFully(ByteBuffer buffer, Selector selector, 
		Date timeoutTime) throws SocketTimeoutException, IOException
	{
		while (buffer.hasRemaining())
		{
			long timeout = timeoutTime.getTime() - System.currentTimeMillis();
			if (timeout <= 0)
				throw new SocketTimeoutException(
					"Timed out trying to read socket channel.");
			if (selector.select(timeout) > 0)
			{
				for (SelectionKey key : selector.selectedKeys())
				{
					SocketChannel channel = (SocketChannel)key.channel();
					channel.read(buffer);
				}
			}
		}
	}
	
	static private int identifyChannel(
		SocketChannel channel, String secretKey, Date timeoutTime)
		throws SocketTimeoutException, IOException
	{
		Selector selector = Selector.open();
		SelectionKey key = null;
		int whichChannel;
		
		ByteBuffer whichChannelBuffer = ByteBuffer.allocate(Byte.SIZE / 8);
		ByteBuffer keyLengthBuffer = ByteBuffer.allocate(Integer.SIZE / 8);
		keyLengthBuffer.order(ByteOrder.nativeOrder());
		
		channel.configureBlocking(false);
		key = channel.register(selector, SelectionKey.OP_READ);
		
		readFully(whichChannelBuffer, selector, timeoutTime);
		whichChannelBuffer.flip();
		whichChannel = (int)whichChannelBuffer.get();
		if ( (whichChannel < 0) || (whichChannel > 2) )
			throw new IOException(
				"Corrupt socket channel found -- invalid channel number.");
		
		readFully(keyLengthBuffer, selector, timeoutTime);
		keyLengthBuffer.flip();
		int length = keyLengthBuffer.getInt();
		ByteBuffer keyBuffer = ByteBuffer.allocate(length);
		readFully(keyBuffer, selector, timeoutTime);
		keyBuffer.flip();
		byte []data = new byte[length];
		keyBuffer.get(data);
		String sKey = new String(data);
		if (!sKey.equals(secretKey))
			throw new IOException(
				"Corrupt socket channel found -- invalid secret key.");
		
		key.cancel();
		channel.configureBlocking(true);
		return whichChannel;
	}
	
	static private SocketChannel[] connectChannels(
		ServerSocketChannel server, String secretKey, Date timeoutTime)
		throws IOException
	{
		Selector selector = Selector.open();
		SocketChannel channel = null;
		SocketChannel []ret = null;
		
		int numConnected = 0;
		SocketChannel []channels = new SocketChannel[] {
				null, null, null
		};
		
		server.register(selector, SelectionKey.OP_ACCEPT);
		
		try
		{
			while (numConnected < 3)
			{
				channel = null;
				long timeLeft = timeoutTime.getTime() - System.currentTimeMillis();
				if (selector.select(timeLeft) > 0)
				{
					try
					{
						channel = server.accept();
						int whichChannel = identifyChannel(
							channel, secretKey, timeoutTime);
						channels[whichChannel] = channel;
						channel = null;
					}
					finally
					{
						StreamUtils.close(channel);
					}
				}
			}
			
			ret = channels;
			channels = null;
			return ret;
		}
		finally
		{
			if (channels != null)
			{
				for (SocketChannel c : channels)
				{
					StreamUtils.close(c);
				}
			}
		}
	}
	
	private void connectStreams(ServerSocketChannel server, 
		String secretKey, Date timeoutTime) throws IOException
	{
		SocketChannel []channels = connectChannels(server, secretKey, timeoutTime);

		Socket []sockets = new Socket[] {
			channels[0].socket(),
			channels[1].socket(),
			channels[2].socket()
		};
		
		sockets[STDIN].shutdownOutput();
		sockets[STDOUT].shutdownInput();
		sockets[STDERR].shutdownInput();
		
		_in = new BufferedReader(new InputStreamReader(
			sockets[STDIN].getInputStream()));
		_out = new PrintWriter(sockets[STDOUT].getOutputStream());
		_err = new PrintWriter(sockets[STDERR].getOutputStream(), true);
	}
	
	GridRunManager(
		ServerSocketChannel server, String secretKey, ITool tool,
		Date timeoutTime) throws IOException
	{
		connectStreams(server, secretKey, timeoutTime);
		_gridTool = tool;
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	public int runTool() throws Throwable
	{
		int ret = _gridTool.run(_out, _err, _in);
	
		_out.flush();
		_err.flush();
		
		return ret;
	}
	
	synchronized public void close() throws IOException
	{
		StreamUtils.close(_in);
		StreamUtils.close(_out);
		StreamUtils.close(_err);
		
		_in = null;
		_out = _err = null;
	}
}