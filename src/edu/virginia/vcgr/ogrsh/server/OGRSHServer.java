package edu.virginia.vcgr.ogrsh.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.client.ApplicationBase;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.ogrsh.server.session.OGRSHContextResolver;
import edu.virginia.vcgr.ogrsh.server.session.SessionManager;

public class OGRSHServer extends ApplicationBase
{
	static private Log _logger = LogFactory.getLog(OGRSHServer.class);

	static public void main(String []args)
	{
		String deploymentName = System.getenv("GENII_DEPLOYMENT_NAME");
		if (deploymentName != null)
		{
			_logger.debug("Using Deployment \"" + deploymentName + "\".");
			System.setProperty(DeploymentName.DEPLOYMENT_NAME_PROPERTY, deploymentName);
		} else
		{
			_logger.debug("Using Deployment \"default\".");
		}
		prepareClientApplication();
		
		boolean _done = false;
		ServerSocketChannel serverChannel = null;
		GUID serverSecret = null;
		
		ContextManager.setResolver(new OGRSHContextResolver());
		SessionManager sessionManager = new SessionManager();
		
		if (args.length > 1)
		{
			System.err.println("USAGE:  OGRSHServer [port]");
			System.exit(1);
		}
		
		try
		{
			serverChannel = ServerSocketChannel.open();
			ServerSocket serverSocket = serverChannel.socket();
			serverSocket.bind(null);
		
			serverSecret = new GUID();
			System.out.println("Server[" + serverSecret + "] listening on port "
				+ serverSocket.getLocalPort());
			serverChannel.configureBlocking(true);
		}
		catch (IOException ioe)
		{
			_logger.fatal("Unable to open server socket.", ioe);
			System.exit(1);
		}
		
		while (!_done)
		{
			try
			{
				SocketChannel socketChannel = serverChannel.accept();
				socketChannel.configureBlocking(true);
				OGRSHConnection connection = new OGRSHConnection(
					sessionManager, socketChannel, serverSecret);
				Thread th = new Thread(connection);
				th.setDaemon(true);
				th.start();
			}
			catch (IOException ioe)
			{
				_logger.error("Error accepting connection.", ioe);
			}
		}
	}
}