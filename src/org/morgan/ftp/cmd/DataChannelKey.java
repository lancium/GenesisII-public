package org.morgan.ftp.cmd;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

public interface DataChannelKey extends Closeable
{
	public String getServerSocketDescription();
	public SocketChannel getChannel(long timeoutSeconds);
}