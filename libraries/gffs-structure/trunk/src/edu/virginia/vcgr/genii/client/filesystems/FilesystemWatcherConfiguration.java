package edu.virginia.vcgr.genii.client.filesystems;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.client.filesystems.script.FilterScriptException;
import edu.virginia.vcgr.genii.client.utils.units.Duration;

class FilesystemWatcherConfiguration
{
	@XmlAttribute(name = "check-period", required = true)
	private String _checkPeriod = null;

	@XmlAttribute(name = "filesystem-name", required = true)
	private String _filesystemName = null;

	@XmlElement(namespace = FilesystemConstants.CONFIGURATION_NS, name = "filter-expression", nillable = true, required = false)
	private FilterExpressionConfiguration _filterExpression = null;

	@XmlElement(namespace = FilesystemConstants.CONFIGURATION_NS, name = "watch-callback", nillable = true, required = false)
	private Collection<WatchCallbackConfiguration> _watchCallback = new LinkedList<WatchCallbackConfiguration>();

	final Duration checkPeriod()
	{
		return new Duration(_checkPeriod);
	}

	final String filesystemName()
	{
		return _filesystemName;
	}

	final FilesystemWatchFilter filter() throws FilterScriptException
	{
		return _filterExpression.filter();
	}

	final Collection<WatchCallbackConfiguration> watchCallback()
	{
		return _watchCallback;
	}
}