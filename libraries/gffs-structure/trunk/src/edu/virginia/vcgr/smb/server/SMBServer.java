package edu.virginia.vcgr.smb.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.HashSet;
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
		// we have found we cannot bind this on 127.0.0.1 directly here, since then we can never mount the drive.
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

	public static Enumeration<NetworkInterface> getNetworks()
	{
		Enumeration<NetworkInterface> nets;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return null;
		}
		return nets;
	}

	public static HashSet<String> getInterfaces(Enumeration<NetworkInterface> nets)
	{
		HashSet<String> toReturn = new HashSet<String>();
		if (nets == null)
			return toReturn;
		while (nets.hasMoreElements()) {
			NetworkInterface net = nets.nextElement();
			Enumeration<InetAddress> inetAddresses = net.getInetAddresses();
			while (inetAddresses.hasMoreElements()) {
				String toAdd = inetAddresses.nextElement().toString();
				if (_logger.isDebugEnabled())
					_logger.debug("adding acceptable interface for this host: '" + toAdd + "'");
				toReturn.add(toAdd);
			}
		}
		return toReturn;
	}

	static HashSet<String> allOurIPAddresses = getInterfaces(getNetworks());

	public boolean okayIPAddress(String ipAddr)
	{
		_logger.debug("vetting host address: " + ipAddr);
		// if (ipAddr.equals("/127.0.0.1"))
		// return true;
		// iterate through hosts we know about here.
		if (allOurIPAddresses.contains(ipAddr)) {
			_logger.debug("okaying ip address that is listed locally: " + ipAddr);
			return true;
		}
		return false;
	}

	@Override
	public void run()
	{
		while (true) {
			try {
				SocketChannel socketChannel = this.serverChannel.accept();
				InetSocketAddress remoteAddr = (InetSocketAddress) socketChannel.getRemoteAddress();
				// String remoteIP = (remoteAddr == null) ? null : remoteAddr.getAddress().toString();
				// only accept connections on localhost, not whole network.
				if ((remoteAddr == null) || !okayIPAddress(remoteAddr.getAddress().toString())) {
					_logger.info("dropping connection from actual remote host: " + remoteAddr);
					socketChannel.close();
					continue;
				} else {
					_logger.debug("accepting connection from this host: " + remoteAddr);
				}

				SMBConnection c = new SMBConnection(socketChannel);
				_logger.info("accepted SMB connection: " + c.toString());
				this.conns.add(c);
				new Thread(c).start();
			} catch (ClosedByInterruptException e) {
				break;
			} catch (IOException e) {
				_logger.error("caught exception in samba server", e);
				// snooze a bit to avoid thrashing if this error keeps happening.
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
				continue;
			}
		}

		try {
			this.serverChannel.close();
		} catch (IOException e) {

		}

		this.listener = null;
	}
}
