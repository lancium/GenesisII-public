package edu.virginia.vcgr.genii.client.filesystems.email;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class ConnectConfig
{
	@XmlAttribute(name = "isSSL", required = false)
	private boolean _isSSL = false;

	@XmlElement(namespace = "http://vcgr.cs.virginia.edu/filesystems/connect", name = "smtp-server", required = true)
	private String _smtpServer;

	@XmlElement(namespace = "http://vcgr.cs.virginia.edu/filesystems/connect", name = "port", required = false)
	private int _port = 25;

	@XmlElement(namespace = "http://vcgr.cs.virginia.edu/filesystems/connect", name = "username", required = false)
	private String _username = null;

	@XmlElement(namespace = "http://vcgr.cs.virginia.edu/filesystems/connect", name = "password", required = false)
	private String _password = null;

	@XmlElement(namespace = "http://vcgr.cs.virginia.edu/filesystems/connect", name = "debug-on", required = false)
	private boolean _debugOn = false;

	final boolean isSSL()
	{
		return _isSSL;
	}

	final String smtpServer()
	{
		return _smtpServer;
	}

	final int port()
	{
		return _port;
	}

	final String username()
	{
		return _username;
	}

	final String password()
	{
		return _password;
	}

	final boolean debugOn()
	{
		return _debugOn;
	}
}
