package edu.virginia.vcgr.genii.container.bes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.net.Hostname;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;

public class BESPWrapperConnection {

	private ExecutorService _pool = Executors.newCachedThreadPool();
	private ServerSocket _server;
	private int _port;
	private String _ipport;

	private Log _besLogger = LogFactory.getLog(BESPWrapperConnection.class);
	private BES _bes;

	// Establishes a socket for communication between the BES and each ProccessWrapper 
	public BESPWrapperConnection(int port, BES bes)
	{
		// Constructor gives the port to use
		_bes = bes;
		try {
			if (port==0) {
				// There is no assigned port yet; get one

				_server = new ServerSocket(0,10);
				_port = _server.getLocalPort();
				_ipport = Hostname.getCurrentIPAddress() + ":" + _port;
				return;
			} else {
				_server = new ServerSocket(port,10);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_port = _server.getLocalPort();
		_ipport = Hostname.getCurrentIPAddress() + ":" + _port;
	}

	public void stop()
	{
		_pool.shutdown();
	}

	public void start()
	{
		startListening();
	}

	private void startListening()
	{
		//need to start a thread to handle this service loop
		Runnable r = new Runnable() {
			public void run() {
				listen();
			};
		};
		_pool.submit(r);
	}

	private void listen()
	{
		while(true)
		{
			try 
			{
				Socket clientSock = _server.accept();

				_besLogger.info("PWrapper Connection Server: Accepting connection from: " + clientSock.getRemoteSocketAddress());

				//need to start another thread to handle this connection
				Runnable r = new Runnable() {
					public void run() {
						handleConnection(clientSock);
					};
				};
				_pool.submit(r);
			} catch (IOException e) 
			{
				_besLogger.error("PWrapper Connection Server: Failed Handling Incoming Connection." + e);
			}
		}
	}

	/*
	 * 2020 July 28 by CCH
	 * 
	 * This implementation of handleConnection checks for a single command,
	 * handles it, then sends the appropriate response. We do not leave
	 * the socket open afterwards.
	 * 
	 * A different implementation would be required if multiple commands 
	 * were being sent with the same socket connection
	 * 
	 * Arguments: Socket created by ServerSocket.accept
	 * handleConnection runs in a new thread
	 */
	private void handleConnection(Socket clientSock)
	{
		_besLogger.info("The hashCode of the clientSock reference: " + clientSock.hashCode());
		try {
			if (_besLogger.isDebugEnabled())
				_besLogger.debug("Setting up input/output streams in handleConnection...");
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
			PrintWriter output = new PrintWriter(clientSock.getOutputStream(), true);
			if (_besLogger.isDebugEnabled())
				_besLogger.debug("Input/output streams setup.");
			try {
				// takes input from the client socket 
				// input.readLine() is a blocking call. We will not proceed until we get input or the socket closes.
				String command = input.readLine();
				_besLogger.info(clientSock.getRemoteSocketAddress() + " says >> " + command);
				// Now we process the commands. All commands are of the form <jobid> command parameters
				// For example "A0C34C26-BC59-09F7-DBA0-BF790D583FA9 register 45335
				// The first thing we do is lookup the activity and ensure that it exists
				if (command !=null && command.length() > 36 && command.length()< 256) {
					String toks[]=command.split(" ");
					if (toks.length <2 || toks.length > 8) {
						_besLogger.error("Invalid activity communication, tokens <2 or >8 with BES from "+toks[0]);
					}
					else {
						// Grab activityid, first token. With activityid, grab activity
						String activityid = toks[0];
						BESActivity activity = _bes.findActivity(activityid);
						if (activity==null) {
							_besLogger.error("Invalid activity ID in communication with BES from "+toks[0]);
						}
						// Handle register command
						// Expected format: "<activityid> register <port>"
						// Rather than having the pwrapper send IP, we grab that from Socket info (easier to implement, no other reason)
						else if (toks[1].equalsIgnoreCase("register")) {
							int port = Integer.parseInt(toks[2]);
							// The following gives us back an IP in form /0.0.0.0.
							// We need to strip the leading "/", or Socket creation will fail later on
							String ipport = clientSock.getInetAddress().toString().substring(1) + ":" + port;
							_besLogger.info("Attempting to set new IPPORT column for " +toks[0] + " to " + ipport);
							try {
								activity.updateIPPort(ipport);
								// Expected result to send back to pwrapper: "<activityid> OK"
								output.println(activityid + " OK");
								output.flush();
							} catch (SQLException e) {
								_besLogger.error("Could not set the new IPPort for "+toks[0] + " to " + ipport, e);
								output.println(activityid + " FAILED");
								output.flush();
							}
						}
						//LAK: 29 Dec 2020: handle terminating command
						// Expected format: <activityid> terminating
						else if(toks[1].equalsIgnoreCase("terminating"))
						{
							activity.notifiyPwrapperIsTerminating();
							
							output.println(activityid + " OK");
							output.flush();
						}
						else if(toks[1].equalsIgnoreCase("persisted"))
						{
							_besLogger.info("PWrapper Connection Server: Got Persisted message");
							activity.notifyPwrapperHasPersisted();
							output.println(activityid + " OK");
							output.flush();
						}
					}
				}
			} catch (IOException e) {
				_besLogger.error("PWrapper Connection Server: Lost Connection to Client. " + e);
				return;
			}

			_besLogger.info("PWrapper Connection Server: Closing connection from: " + clientSock.getRemoteSocketAddress());

			// close connection
			clientSock.close(); 
			input.close(); 
			output.close();
		}
		catch (IOException e) {
			_besLogger.error("PWrapper Connection Server: Could not handle connection. " + e);
		}
	}
	
	public String getSocketPort() {
		return new Integer(_server.getLocalPort()).toString();
	}
	public String getIPPort() {
		return _ipport;
	}
}
