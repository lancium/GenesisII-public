package org.morgan.ftp.backends.local;

import java.io.File;

import org.morgan.ftp.FTPConfiguration;
import org.morgan.ftp.FTPDaemon;

public class Driver
{
	static public void main(String []args) throws Throwable
	{
		if (args.length != 1)
		{
			System.err.println("USAGE:  Driver <listen-port>");
			System.exit(1);
		}
		
		int port = Integer.parseInt(args[0]);
		
		LocalBackendConfiguration backendConfiguration = new LocalBackendConfiguration();
		backendConfiguration.addUser("mmm2a", "password", new File("/home/mmm2a/ftp"), true, true);
		
		LocalBackendFactory backendFactory = new LocalBackendFactory(backendConfiguration);
		
		FTPConfiguration ftpConfiguration = new FTPConfiguration(port);
		FTPDaemon daemon = new FTPDaemon(backendFactory, ftpConfiguration);
		daemon.start();
		System.out.println("Listening on port " + port);
		
		while (daemon.isRunning())
		{
			try { Thread.sleep(1000 * 5); } catch (Throwable cause) {}
		}
	}
}