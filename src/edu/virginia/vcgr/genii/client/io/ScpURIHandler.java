package edu.virginia.vcgr.genii.client.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import edu.virginia.vcgr.genii.client.io.scp.ScpUtility;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.UsernamePasswordIdentity;

public class ScpURIHandler implements IURIHandler
{
	static private final String []_HANDLED_PROTOCOLS =
		new String[] { "scp" };
	
	@Override
	public boolean canRead(String uriScheme)
	{
		return true;
	}

	@Override
	public boolean canWrite(String uriScheme)
	{
		return true;
	}

	@Override
	public String[] getHandledProtocols()
	{
		return _HANDLED_PROTOCOLS;
	}

	@Override
	public void get(URI source, File target, UsernamePasswordIdentity credential)
			throws IOException
	{
		String user = null;
		String password = null;
		String host = null;
		int port;
		String remotePath = null;
		
		if (credential == null)
			throw new IOException(
				"No authentication information provided for URL \"" 
				+ source + "\".");
		
		user = credential.getUserName();
		password = credential.getPassword();
		
		if (user == null)
			throw new IOException(
				"No authentication information provided for URL \"" 
				+ source + "\".");
		
		if (password == null)
			throw new IOException(
				"No passwrod given for URL \"" + source + "\".");
		
		host = source.getHost();
		port = source.getPort();
		remotePath = source.getPath();
		
		if (host == null)
			throw new IOException("No host given for URL \"" +
				source + "\".");
		if (remotePath == null)
			throw new IOException("No path given for URL \"" +
				source + "\".");
		
		if (port < 0)
			port = 22;
		
		ScpUtility.get(target, user, password, host, port, remotePath);
	}

	@Override
	public void put(File source, URI target, UsernamePasswordIdentity credential)
			throws IOException
	{
		String user = null;
		String password = null;
		String host = null;
		int port;
		String remotePath = null;
		
		if (credential == null)
			throw new IOException(
				"No authentication information provided for URL \"" 
				+ source + "\".");
		
		user = credential.getUserName();
		password = credential.getPassword();
		
		if (user == null)
			throw new IOException(
				"No authentication information provided for URL \"" 
				+ source + "\".");
		
		if (password == null)
			throw new IOException(
				"No passwrod given for URL \"" + source + "\".");
		
		host = target.getHost();
		port = target.getPort();
		remotePath = target.getPath();
		
		if (host == null)
			throw new IOException("No host given for URL \"" +
				source + "\".");
		if (remotePath == null)
			throw new IOException("No path given for URL \"" +
				source + "\".");
		
		if (port < 0)
			port = 22;
		
		ScpUtility.put(source, user, password, host, port, remotePath);
	}
}