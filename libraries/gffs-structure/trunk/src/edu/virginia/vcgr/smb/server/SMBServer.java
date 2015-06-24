package edu.virginia.vcgr.smb.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SMBServer implements Runnable
{
	static private Log _logger = LogFactory.getLog(SMBServer.class);

	private Thread listener;
	private int port;
	private ServerSocketChannel serverChannel;
	private List<SMBConnection> conns;

	public SMBServer(int port)
	{
		this.port = port;
		this.conns = new LinkedList<SMBConnection>();
	}

	public void start() throws IOException
	{
		_logger.info("starting SMB server up.");
		if (this.listener != null)
			return;

		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.bind(new InetSocketAddress(this.port));
		this.listener = new Thread(this);
		this.listener.start();
		_logger.info("SMB server is now running.");
	}

	public void stop()
	{
		_logger.info("shutting down SMB server.");
		for (SMBConnection conn : conns) {
			conn.stop();
		}

		conns.clear();

		this.listener.interrupt();
		_logger.info("SMB server has been stopped.");
	}

	@Override
	public void run()
	{
		while (true) {
			try {
				SocketChannel socketChannel = this.serverChannel.accept();

				SMBConnection c = new SMBConnection(socketChannel);
				_logger.info("accepted SMB connection: " + c.toString());
				this.conns.add(c);
				new Thread(c).start();
			} catch (ClosedByInterruptException e) {
				break;
			} catch (IOException e) {
				return;
			}
		}

		try {
			this.serverChannel.close();
		} catch (IOException e) {

		}

		this.listener = null;
	}
}
