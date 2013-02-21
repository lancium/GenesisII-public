package edu.virginia.vcgr.genii.client.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.io.gsiftp.GsiFtpUtility;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public class GsiFtpURIHandler extends AbstractURIHandler
{
	static private final String []_HANDLED_PROTOCOLS =
		new String[] { "gsiftp" };
	static private int DEFAULT_PORT = 2811;
	
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
	protected DataTransferStatistics getInternal(
		URI source, File destination, UsernamePasswordIdentity credential)
			throws IOException
	{
		String host = null;
		int port;
		String localPath = null;
		host = source.getHost();
		port = source.getPort();
		localPath = source.getPath();
		
		if(! new File(destination.getParentFile().getAbsolutePath() + GenesisIIConstants.myproxyFilenameSuffix).exists()){
			throw new IOException("No X509 found for the user. Try resubmitting the job after performing an xsedeLogin");
		}
		
		if (host == null)
			throw new IOException("No host given for URL \"" +
				source + "\".");
		if (localPath == null)
			throw new IOException("No path given for URL \"" +
				source + "\".");
		
		if (port < 0)
			port = DEFAULT_PORT;
		
		return GsiFtpUtility.get(destination, host, port, localPath);
	}

	@Override
	protected DataTransferStatistics putInternal(
		File source, URI target, UsernamePasswordIdentity credential)
			throws IOException
	{
		String host = null;
		int port;
		String remotePath = null;
		host = target.getHost();
		port = target.getPort();
		remotePath = target.getPath();
		
		if(! new File(source.getParentFile().getAbsolutePath() + GenesisIIConstants.myproxyFilenameSuffix).exists()){
			throw new IOException("No X.509 certificate found for the user. Try resubmitting the job after performing xsedeLogin command");
		}
		
		if (host == null)
			throw new IOException("No host given for URL \"" +
				source + "\".");
		if (remotePath == null)
			throw new IOException("No path given for URL \"" +
				source + "\".");
		
		if (port < 0)
			port = DEFAULT_PORT;
		
		return GsiFtpUtility.put(source, host, port, remotePath);
	}

	@Override
	public InputStream openInputStream(URI source,
			UsernamePasswordIdentity credential) throws IOException
	{
		throw new RuntimeException(
			"openInputStream should never be called on a GSIFTPURIHandler.");
	}

	@Override
	public OutputStream openOutputStream(URI target,
			UsernamePasswordIdentity credential) throws IOException
	{
		throw new RuntimeException(
		"openOutputStream should never be called on a GSIFTPURIHandler.");
	}
}