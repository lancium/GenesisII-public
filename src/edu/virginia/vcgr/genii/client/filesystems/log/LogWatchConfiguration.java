package edu.virginia.vcgr.genii.client.filesystems.log;

import javax.xml.bind.annotation.XmlElement;

class LogWatchConfiguration
{
	@XmlElement(
		namespace = "http://vcgr.cs.virginia.edu/filesystems/log-watch", 
		name = "message", required = true)
	private String _messageFormat = null;
	
	final String format(Object...args)
	{
		return String.format(_messageFormat, args);
	}
}