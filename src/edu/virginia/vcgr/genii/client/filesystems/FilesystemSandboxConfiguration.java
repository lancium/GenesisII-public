package edu.virginia.vcgr.genii.client.filesystems;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;

import org.morgan.util.MacroUtils;

class FilesystemSandboxConfiguration
{
	@XmlAttribute(name = "name", required = true)
	private String _name = null;
	
	@XmlAttribute(name = "relative-path", required = true)
	private String _relativePath = null;
	
	@XmlAttribute(name = "create", required = false)
	private boolean _create = true;
	
	@SuppressWarnings("unused")
	private void afterUnmarshal(Unmarshaller u, Object parent)
	{
		MacroUtils.replaceMacros(System.getProperties(), _relativePath);
	}
	
	final String name()
	{
		return _name;
	}
	
	final String relativePath()
	{
		return _relativePath;
	}
	
	final boolean doCreate()
	{
		return _create;
	}
}