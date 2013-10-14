package edu.virginia.vcgr.genii.client.version;

import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.appmgr.version.Version;

public class MinimumVersionException extends RemoteException
{
	static final long serialVersionUID = 0L;

	static private final String ERROR_MESSAGE_FORMAT = "Client (%s) does not meet the minumum version requirements (%s).";

	static private final Pattern EXTRACTOR = Pattern.compile("^.*\\(([^\\)]+)\\).+\\(([^\\)]+)\\).*$");

	private Version _clientVersion;
	private Version _minimumVersion;

	public MinimumVersionException(Version clientVersion, Version minimumVersion)
	{
		super(String.format(ERROR_MESSAGE_FORMAT, clientVersion, minimumVersion));

		_clientVersion = clientVersion;
		_minimumVersion = minimumVersion;
	}

	final public Version getClientVersion()
	{
		return _clientVersion;
	}

	final public Version getMinimumVersion()
	{
		return _minimumVersion;
	}

	static public MinimumVersionException reformException(String message)
	{
		Matcher matcher = EXTRACTOR.matcher(message);
		if (matcher.matches())
			return new MinimumVersionException(new Version(matcher.group(1)), new Version(matcher.group(2)));

		return null;
	}
}