package edu.virginia.vcgr.genii.container.bes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	private HashMap<String, String> _pwrapperSocketsInfo;

	// Establishes a socket for communication between the BES and each ProccessWrapper 
	public BESPWrapperConnection(int port, BES bes)
	{
		// Constructor gives the port to use
		_pwrapperSocketsInfo = new HashMap<String, String>(1);
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

	private void handleConnection(Socket clientSock)
	{
		_besLogger.info("The hashCode of the clientSock reference: " + clientSock.hashCode());
		String command;
		String activityid = null;
		try
		{
			// takes input from the client socket 
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
			PrintWriter output = new PrintWriter(clientSock.getOutputStream(), true);
			//////All of the following block is temporary for testing///////
			while(clientSock.isConnected() && !clientSock.isClosed()) {
				try {
					while ((command = input.readLine()) != null) {
						_besLogger.info(clientSock.getRemoteSocketAddress() + " says >> " + command);
						// Now we process the commands. All commands are of the form <jobid> command parameters
						// For example "A0C34C26-BC59-09F7-DBA0-BF790D583FA9 register 192.3.4.211:45335
						// The first thing we do is lookup the activity and ensure that it exists
						if (command !=null && command.length() > 36 && command.length()< 256) {
							String toks[]=command.split(" ");
							if (toks.length <2 || toks.length > 8) {
								_besLogger.error("Invalid activity communication, tokens <3 or >8 with BES from "+toks[0]);
								break;
							}
							// Lookup toks[0]
							BESActivity activity=_bes.findActivity(toks[0]);
							if (activity==null) {
								_besLogger.error("Invalid activity ID in communication with BES from "+toks[0]);
								break;
							}
							if (toks[1].equalsIgnoreCase("register")) {
								activityid = toks[0];
								putSocketInfo(activityid, toks[2]);
								_besLogger.error("Attempting to set new IPPORT column for "+toks[0] + " to " + toks[2]);
								try {
									activity.updateIPPort(toks[2]);
								} catch (SQLException e) {
									_besLogger.error("Could not set the new IPPort for "+toks[0] + " to " + toks[2], e);
									output.println(activityid + " OK");
									output.flush();
								}
							}

						}
						output.println(command);
						if (command.equals("Bye."))
							break;
					}
				} catch (IOException e) {
					_besLogger.error("PWrapper Connection Server: Lost Connection to Client. " + e);
					return;
				}
			}
			///////////////////////////////////////////////////////////////

			_besLogger.info("PWrapper Connection Server: Closing connection from: " + clientSock.getRemoteSocketAddress());

			// close connection 
			clientSock.close(); 
			input.close(); 
			output.close();
		}
		catch (IOException e) {
			System.out.println(e); 
		}
	}
	

	public boolean sendCommand(String activityid, String commandToSend) {
		// commandToSend should also contain activityid
		_besLogger.info("SendCommand called with command: " + commandToSend);
		String socketInfo = getSocketInfo(activityid);
		if (socketInfo == null) return false;
		String[] ipport = socketInfo.split(":");
		String ipaddr = ipport[0];
		int port = Integer.parseInt(ipport[1]);
		Socket socket = null;
		try {
			socket = new Socket(ipaddr, port);
		} catch (IOException e) {
			_besLogger.error("Unable to set up socket connection with " + ipaddr + ":" + port + ".", e);
		}
		boolean success = false;
		try
		{
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
			String response = null;
			output.println(commandToSend);
			output.flush();
			while ((response = input.readLine()) != null) {
				success = response.equals(activityid + " OK");
				if (success) break;
			}
			socket.close();
		}
		catch (IOException e) {
			_besLogger.error("Exception occurred while handling " + commandToSend, e);
			return false;
		}
		return success;
	}

	public String getSocketPort() {
		return new Integer(_server.getLocalPort()).toString();
	}
	public String getIPPort() {
		return _ipport;
	}
	private void putSocketInfo(String activityid, String socket) {
		synchronized(_pwrapperSocketsInfo) {
			if (!_pwrapperSocketsInfo.containsKey(activityid))
				_pwrapperSocketsInfo.put(activityid, socket);
		}
	}
	public void removeSocketInfo(String activityid) {
		synchronized(_pwrapperSocketsInfo) {
			if (_pwrapperSocketsInfo.containsKey(activityid))
				_pwrapperSocketsInfo.remove(activityid);
		}
	}
	private String getSocketInfo(String activityid) {
		synchronized(_pwrapperSocketsInfo) {
			return _pwrapperSocketsInfo.get(activityid);
		}
	}
}
