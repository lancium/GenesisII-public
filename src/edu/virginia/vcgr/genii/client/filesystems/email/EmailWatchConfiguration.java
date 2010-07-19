package edu.virginia.vcgr.genii.client.filesystems.email;

import javax.xml.bind.annotation.XmlElement;

public class EmailWatchConfiguration 
{
	@XmlElement (
		namespace = "http://vcgr.cs.virginia.edu/filesystems/connect",
		name = "connection", required = true)
	private ConnectConfig _connection = null;
	
	@XmlElement (
		namespace = "http://vcgr.cs.virginia.edu/filesystems/address-info",
		name = "addressing-info", required = true)
	private AddressInfoConfiguration _addr = null;
	
	@XmlElement (
		namespace = "http://vcgr.cs.virginia.edu/filesystems/email-watch",
		name = "subject", required = false)
	private String _subject = null;
	
	@XmlElement (
		namespace = "http://vcgr.cs.virginia.edu/filesystems/email-watch",
		name = "message", required = false)
	private String _messageFormat = null;
	
	String message()
	{
		return _messageFormat;
	}
		
	String subject()
	{
		return _subject;
	}
	
	AddressInfoConfiguration addr()
	{
		return _addr;
	}
	
	ConnectConfig connection()
	{
		return _connection;
	}
	
	final String format(Object...args)
	{
		return String.format(_messageFormat, args);
	}
	
}