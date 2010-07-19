package edu.virginia.vcgr.genii.client.filesystems;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAnyElement;

import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.filesystems.script.FilesystemScriptFilter;
import edu.virginia.vcgr.genii.client.filesystems.script.FilterScriptException;

class FilterExpressionConfiguration
{
	@XmlAnyElement
	private Collection<Element> _any = null;
	
	final FilesystemWatchFilter filter()
		throws FilterScriptException
	{
		if (_any == null || _any.size() == 0)
			return FilesystemScriptFilter.constantFilter(true);

		if (_any.size() != 1)
			throw new FilterScriptException(
				"Filesystem filter expressions MUST have no " +
				"more then 1 child element.");
		
		return FilesystemScriptFilter.parseScript(_any.iterator().next());
	}
}
