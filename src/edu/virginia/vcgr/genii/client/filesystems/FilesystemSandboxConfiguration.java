package edu.virginia.vcgr.genii.client.filesystems;

import javax.xml.bind.annotation.XmlAttribute;

class FilesystemSandboxConfiguration
{
	@XmlAttribute(name = "name", required = true)
	private String _name = null;
	
	@XmlAttribute(name = "relative-path", required = true)
	private String _relativePath = null;
	
	final String name()
	{
		return _name;
	}
	
	final String relativePath()
	{
		return _relativePath;
	}
}