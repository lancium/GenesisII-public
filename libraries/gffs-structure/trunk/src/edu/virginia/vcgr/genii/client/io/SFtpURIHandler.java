package edu.virginia.vcgr.genii.client.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.morgan.util.io.DataTransferStatistics;

import edu.virginia.vcgr.genii.client.io.scp.ScpUtility;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public class SFtpURIHandler extends AbstractURIHandler
{
	static private final String[] _HANDLED_PROTOCOLS = new String[] { "sftp" };

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
	protected DataTransferStatistics getInternal(URI source, File target, UsernamePasswordIdentity credential)
		throws IOException
	{
		String user = null;
		String password = null;
		String host = null;
		int port;
		String remotePath = null;

		if (credential == null)
			throw new IOException("No authentication information provided for URL \"" + source + "\".");

		user = credential.getUserName();
		password = credential.getPassword();

		if (user == null)
			throw new IOException("No authentication information provided for URL \"" + source + "\".");

		if (password == null)
			throw new IOException("No passwrod given for URL \"" + source + "\".");

		host = source.getHost();
		port = source.getPort();
		remotePath = source.getPath();

		if (host == null)
			throw new IOException("No host given for URL \"" + source + "\".");
		if (remotePath == null)
			throw new IOException("No path given for URL \"" + source + "\".");

		if (port < 0)
			port = 22;

		target.createNewFile();
		return ScpUtility.get(target, user, password, host, port, remotePath, true);
	}

	@Override
	protected DataTransferStatistics putInternal(File source, URI target, UsernamePasswordIdentity credential)
		throws IOException
	{
		String user = null;
		String password = null;
		String host = null;
		int port;
		String remotePath = null;

		if (credential == null)
			throw new IOException("No authentication information provided for URL \"" + source + "\".");

		user = credential.getUserName();
		password = credential.getPassword();

		if (user == null)
			throw new IOException("No authentication information provided for URL \"" + source + "\".");

		if (password == null)
			throw new IOException("No passwrod given for URL \"" + source + "\".");

		host = target.getHost();
		port = target.getPort();
		remotePath = target.getPath();

		if (host == null)
			throw new IOException("No host given for URL \"" + source + "\".");
		if (remotePath == null)
			throw new IOException("No path given for URL \"" + source + "\".");

		if (port < 0)
			port = 22;

		return ScpUtility.put(source, user, password, host, port, remotePath, true);
	}

	@Override
	public InputStream openInputStream(URI source, UsernamePasswordIdentity credential) throws IOException
	{
		throw new RuntimeException("openInputStream should never be called on a ScpURIHandler.");
	}

	@Override
	public OutputStream openOutputStream(URI target, UsernamePasswordIdentity credential) throws IOException
	{
		throw new RuntimeException("openOutputStream should never be called on a ScpURIHandler.");
	}
}