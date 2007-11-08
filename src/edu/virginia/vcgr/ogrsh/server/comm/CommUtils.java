package edu.virginia.vcgr.ogrsh.server.comm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class CommUtils
{
	static public void readFully(SocketChannel channel, ByteBuffer buffer)
		throws IOException
	{
		while (buffer.remaining() > 0)
		{
			if (channel.read(buffer) <= 0)
				throw new IOException("Socket connection closed by client.");
		}
	}
	
	static public void writeFully(SocketChannel channel, ByteBuffer buffer)
		throws IOException
	{
		while (buffer.remaining() > 0)
		{
			if (channel.write(buffer) <= 0)
				throw new IOException("Socket connection closed by client.");
		}
	}
}