package org.morgan;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import edu.virginia.vcgr.genii.client.io.scp.ScpUtility;

public class TestScp
{
	static private void usage()
	{
		System.err.println("USAGE:  TestScp <source> <target> <user> <password>");
		System.exit(1);
	}
	
	static private String getProtocol(String str)
	{
		int lcv = str.indexOf("://");
		if (lcv > 0)
			return str.substring(0, lcv);
		
		return null;
	}
	
	static private void get(URI sourceURI, String target, 
		String user, String password, String protocol) throws IOException
	{
		ScpUtility.get(new File(target), user, password,
			sourceURI.getHost(), 22, sourceURI.getPath(),
			protocol.equals("sftp"));
	}
	
	static private void put(String source, URI targetURI, 
		String user, String password, String protocol) throws IOException
	{
		ScpUtility.put(new File(source), user, password, 
			targetURI.getHost(), 22, targetURI.getPath(),
			protocol.equals("sftp"));
	}
	
	static public void main(String []args) throws Throwable
	{
		if (args.length != 4)
			usage();
		
		String source = args[0];
		String target = args[1];
		String user = args[2];
		String password = args[3];
		
		String protocol = getProtocol(source);
		if (protocol != null)
			get(URI.create(source), target, user, password, protocol);
		else
		{
			protocol = getProtocol(target);
			if (protocol != null)
				put(source, URI.create(target), user, password, protocol);
			else
				usage();
		}
	}
}