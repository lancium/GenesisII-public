package edu.virginia.vcgr.ogrsh.server.gtool;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Date;

import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.ITool;
import edu.virginia.vcgr.genii.client.cmd.ToolException;

public class GridRunDescription implements Closeable
{
	private ITool _gridTool;
	private String _secretKey;
	private ServerSocketChannel _server;
	
	@SuppressWarnings("unchecked")
	public GridRunDescription(String gridToolClassName, String []arguments)
		throws ClassNotFoundException, IllegalAccessException, 
			InstantiationException, ToolException, IOException
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class<? extends ITool> toolClass = 
			(Class<? extends ITool>)loader.loadClass(gridToolClassName);
		_gridTool = toolClass.newInstance();
		
		for (String arg : arguments)
			_gridTool.addArgument(arg);
		
		_secretKey = (new GUID()).toString();
		
		_server = ServerSocketChannel.open();
		_server.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));
		_server.configureBlocking(false);
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	synchronized public int getPort()
	{
		if (_server == null)
			throw new IllegalStateException(
				"Cannot get port after run description is closed.");
		
		return _server.socket().getLocalPort();
	}
	
	public String getSecretKey()
	{
		return _secretKey;
	}
	
	synchronized public void close() throws IOException
	{
		if (_server != null)
			StreamUtils.close(_server);
		_server = null;
	}
	
	synchronized public GridRunManager prepare(Date timeoutTime)
		throws IOException
	{
		if (_server == null)
			throw new IllegalStateException(
				"Cannot prepare grid run after run description is closed.");
		
		try
		{
			return new GridRunManager(
				_server, _secretKey, _gridTool, timeoutTime);
		}
		finally
		{
			StreamUtils.close(_server);
			_server = null;
		}
	}
	
	public GridRunManager prepare(long timeout)
		throws IOException
	{
		return prepare(new Date(System.currentTimeMillis() + timeout));
	}
}