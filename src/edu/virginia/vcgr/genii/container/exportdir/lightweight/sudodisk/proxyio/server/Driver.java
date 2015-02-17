package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.Constants;

/**
 * IMPORTANT: Do not write to stdout other than what's already there! Use PSFileWriter.writeToFile
 * to write to file and debug there! This is because stdout is piped back to the parent. Writing to
 * stdout continuously will cause threads in this process to block waiting for parent to read (out
 * of the pipe), and that will never happen causing a stall!
 * 
 * The stdouts that exist in main are for writing port number back to parent
 * 
 * @author avinash
 */
public class Driver
{
	static private Log _logger = LogFactory.getLog(Driver.class);

	public static void main(String[] args)
	{
		_logger.info("starting proxy io driver program...");

		BufferedInputStream bis = new BufferedInputStream(System.in);
		_logger.info("Proxy server: request to start");

		// int nonce = -1;
		byte[] nonce = new byte[Constants.NONCE_SIZE];
		try {
			int nonce_size = bis.read(nonce);
			// We ensure we get 16 bytes from client!
			if (nonce_size != Constants.NONCE_SIZE) {
				System.out.println("0");
				_logger.info("ProxyServer: Did not get 16 byte nonce. Exitting");
				System.exit(-1);
			}
			bis.close();

		} catch (Exception e) {
			System.out.println("0");
			_logger.info("ProxyServer: Error reading nonce. Exiting!");
			System.exit(-1);
		}

		// open server socket
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
		} catch (IOException ioe) {
			StreamUtils.close(socket);
			System.out.println("0");
			_logger.info("ProxyServer: Error creating server socket. Exiting!!");
			System.exit(-1);
		}

		_logger.info("ProxyServer: Proxy started on port: " + socket.getLocalPort());

		System.out.println(socket.getLocalPort());
		
		_logger.info("communicated port to client.");
		
		// start the monitoring thread!
		Monitor monitor = new Monitor(System.currentTimeMillis());
		Thread monitor_thread = new Thread(monitor);
		monitor_thread.start();
		
		_logger.info("started monitoring thread.");

		_logger.info("before starting accept loop.");
		
		// Starting select-accept loop!
		while (true) {
			try {
				// wait for request
				Socket connxn = socket.accept();
				//hmmm: this should only accept on loopback, and then we don't need a check.
				
				//hmmm: too noisy.
				_logger.info("accept gives us a client socket on port " + connxn.getPort());				

				// Only localhost requests are entertained!
				if (!connxn.getInetAddress().isLoopbackAddress()) {
					_logger.warn("Not loopback request - ignoring!!  address is: " + connxn.getInetAddress().getHostAddress());
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