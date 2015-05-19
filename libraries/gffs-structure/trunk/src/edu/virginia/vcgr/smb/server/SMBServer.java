package edu.virginia.vcgr.smb.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

public class SMBServer implements Runnable {
	private Thread listener;
	private int port;
	private ServerSocketChannel serverChannel;
	private List<SMBConnection> conns;
	
	public SMBServer(int port) {
		this.port = port;
		this.conns = new LinkedList<SMBConnection>();
	}
	
	public void start() throws IOException {
		if (this.listener != null)
			return;
		
		this.serverChannel = ServerSocketChannel.open();
		this.serverChannel.bind(new InetSocketAddress(this.port));
		this.listener = new Thread(this);
		this.listener.start();
	}
	
	public void stop() {
		for (SMBConnection conn: conns) {
			conn.stop();
		}
		
		conns.clear();
		
		this.listener.interrupt();
	}

	@Override
	public void run() {
		while (true) {
			try {
				SocketChannel socketChannel = this.serverChannel.accept();
			
				SMBConnection c = new SMBConnection(socketChannel);
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
