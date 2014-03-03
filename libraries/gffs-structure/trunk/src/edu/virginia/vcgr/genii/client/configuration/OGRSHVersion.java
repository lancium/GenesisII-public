package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;

public class OGRSHVersion
{
	private String _version;
	private File _shimScript;

	public OGRSHVersion(String version, File shimScript)
	{
		_version = version;
		_shimScript = shimScript;
	}

	public File shimScript()
	{
		return _shimScript;
	}

	public String version()
	{
		return toString();
	}

	public String toString()
	{
		return _version;
	}
}