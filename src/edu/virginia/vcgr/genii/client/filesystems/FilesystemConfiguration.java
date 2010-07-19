package edu.virginia.vcgr.genii.client.filesystems;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

class FilesystemConfiguration
{
	@XmlAttribute(name = "name", required = true)
	private String _name = null;
	
	@XmlAttribute(name = "path", required = true)
	private String _path = null;
	
	@XmlElement(namespace = FilesystemConstants.CONFIGURATION_NS,
		name = "filesystem-property", nillable = true, required = false)
	private Collection<FilesystemProperties> _properties =
		new LinkedList<FilesystemProperties>();
	
	@XmlElement(namespace = FilesystemConstants.CONFIGURATION_NS,
		name = "filesystem-sandbox", required = false)
	private Collection<FilesystemSandboxConfiguration> _sandboxes =
		new LinkedList<FilesystemSandboxConfiguration>();
	
	final String name() 
	{
		return _name;
	}
	
	final String path()
	{
		return _path;
	}
	
	final Collection<FilesystemProperties> properties()
	{
		return Collections.unmodifiableCollection(_properties);
	}
	
	final Collection<FilesystemSandboxConfiguration> sandboxes()
	{
		return Collections.unmodifiableCollection(_sandboxes);
	}
}