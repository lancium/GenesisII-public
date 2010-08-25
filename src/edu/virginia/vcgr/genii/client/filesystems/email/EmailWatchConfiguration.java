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
	
	@XmlElement(
		namespace = "http://vcgr.cs.virginia.edu/filesystems/email-watch",
		name = "positive-subject", required = false)
	private String _positiveSubject = null;
	
	@XmlElement (
		namespace = "http://vcgr.cs.virginia.edu/filesystems/email-watch",
		name = "message", required = false)
	private String _message = null;
	
	@XmlElement (
		namespace = "http://vcgr.cs.virginia.edu/filesystems/email-watch",
		name = "positive-message", required = false)
	private String _positiveMessage = null;
	
	String message(boolean isNegative)
	{
		if (isNegative || _positiveMessage == null)
			return _message;
		return _positiveMessage;
	}
	
	String subject(boolean isNegative)
	{
		if (isNegative || _positiveMessage == null)
			return _subject;
		return _positiveSubject;
	}
	
	AddressInfoConfiguration addr()
	{
		return _addr;
	}
	
	ConnectConfig connection()
	{
		return _connection;
	}
}