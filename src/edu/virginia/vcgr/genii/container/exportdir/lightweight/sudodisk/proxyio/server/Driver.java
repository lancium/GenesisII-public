package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.Constants;

/**
 * IMPORTANT: Do not write to stdout other than what's already there! Use PSFileWriter.writeToFile to write to file and debug there! This is
 * because stdout is piped back to the parent. Writing to stdout continuously will cause threads in this process to block waiting for parent
 * to read (out of the pipe), and that will never happen causing a stall!
 * 
 * The stdouts that exist in main are for writing port number back to parent
 * 
 * @author avinash
 */
public class Driver
{
	static private Log _logger = LogFactory.getLog(Driver.class);

	static final int SOCKET_BACKLOG = 50;

	public static void main(String[] args)
	{
		if (_logger.isDebugEnabled())
			_logger.debug("Proxy server: request to start");

		BufferedInputStream bis = new BufferedInputStream(System.in);

		byte[] nonce = new byte[Constants.NONCE_SIZE];
		try {
			int nonce_size = bis.read(nonce);
			// We ensure we get 16 bytes from client!
			if (nonce_size != Constants.NONCE_SIZE) {
				System.out.println("0");
				_logger.error("ProxyServer: Did not get 16 byte nonce. Exiting!");
				System.exit(-1);
			}
			bis.close();

		} catch (Exception e) {
			System.out.println("0");
			_logger.error("ProxyServer: Problem reading nonce. Exiting!");
			System.exit(-1);
		}

		// open server socket
		ServerSocket socket = null;
		try {
			InetAddress addr = InetAddress.getByName("127.0.0.1");
			socket = new ServerSocket(0, SOCKET_BACKLOG, addr);
		} catch (IOException ioe) {
			StreamUtils.close(socket);
			System.out.println("0");
			_logger.error("ProxyServer: Error creating server socket.  Exiting!", ioe);
			System.exit(-1);
		}

		_logger.info("ProxyServer: Proxy started on port: " + socket.getLocalPort());

		System.out.println(socket.getLocalPort());

		if (_logger.isDebugEnabled())
			_logger.debug("communicated port to client.");

		// start the monitoring thread.
		Monitor monitor = new Monitor(System.currentTimeMillis());
		Thread monitor_thread = new Thread(monitor);
		monitor_thread.start();
		if (_logger.isDebugEnabled())
			_logger.debug("started monitoring thread.");

		if (_logger.isDebugEnabled())
			_logger.debug("before starting accept loop.");

		// Starting select-accept loop.
		while (true) {
			try {
				// wait for request
				Socket connxn = socket.accept();
				if (_logger.isDebugEnabled())
					_logger.debug("accept gives us a client socket on port " + connxn.getPort());

				/*
				 * only localhost requests are supported. this should be ensured by construction of server socket above, but we're still
				 * checking just to make sure we never see a connection from elsewhere.
				 */
				if (!connxn.getInetAddress().isLoopbackAddress()) {
					_logger.error("Not loopback request - ignoring!  (how did this happen?)  address is: "
						+ connxn.getInetAddress().getHostAddress());
					continue;
				} else {
					Monitor.setLastReqTime(System.currentTimeMillis());
				}

				RequestHandler reqHandler = new RequestHandler(connxn, nonce);
				Thread t = new Thread(reqHandler);
				t.start();
			} catch (Exception e) {
				_logger.warn("exception in main loop of proxy io server.", e);
			}
		}
	}

	public static void write(String s) throws Exception
	{
		BufferedOutputStream bos = new BufferedOutputStream(System.out);
		s = s + "\n";
		byte[] b = s.getBytes();
		bos.write(b);
		bos.flush();
	}

}