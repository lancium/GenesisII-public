package edu.virginia.vcgr.genii.container.bes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BESPWrapperConnection {
	
	private ExecutorService _pool = Executors.newCachedThreadPool();
	private ServerSocket _server;
	
	private Log _besLogger = LogFactory.getLog(BESPWrapperConnection.class);
	
	// Establishes a socket for communication between the BES and each ProccessWrapper 
	public BESPWrapperConnection(int port, int max)
	{
		if (port > max)
			_besLogger.fatal("Port range start must be less than port range end. Bad port range: " + port + " to " + max);
		for (; port <= max; port++) {
			try
			{
				_server = new ServerSocket(port);
				_besLogger.info("PWrapper Connection Server: Started on port " + String.valueOf(port));
				break;
			} catch (IOException e) {
				if (port != max)_besLogger.error("Could not start PWrapperConnection Server.", e);
				else
					_besLogger.fatal("Could not start PWrapperConnection Server on any port between " + port + " and " + max);
			}
		}
		
		startListening();
	}
	
	public void stop()
	{
		_pool.shutdown();
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
		try
        {
            // takes input from the client socket 
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
            OutputStream output = clientSock.getOutputStream();
            //////All of the following block is temporary for testing///////
            while(clientSock.isConnected() && !clientSock.isClosed()) {
                try {
                    String line = input.readLine();
                    
                    if(line.equals("close") || line == null)
                    {
                        break;
                    }
                    else
                    {
                    	//temp
                    	_besLogger.info(clientSock.getRemoteSocketAddress() + " says >> " + line);
                    	output.write(("PWrapper Connection Server: Sending back identical string: " + line).getBytes(Charset.forName("UTF-8")));
                    	output.flush();
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
	
	public String getSocketPort() {
		return new Integer(_server.getLocalPort()).toString();
	}
}
