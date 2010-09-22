package edu.virginia.vcgr.genii.client.filesystems;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.morgan.util.macro.MacroUtils;

class FilesystemConfiguration extends FilesystemSandboxContainerConfiguration
{
	@XmlAttribute(name = "path", required = true)
	private String _path = null;
	
	@XmlElement(namespace = FilesystemConstants.CONFIGURATION_NS,
		name = "filesystem-property", nillable = true, required = false)
	private Collection<FilesystemProperties> _properties =
		new LinkedList<FilesystemProperties>();
	
	@SuppressWarnings("unused")
	private void afterUnmarshal(Unmarshaller u, Object parent)
	{
		_path = MacroUtils.replaceMacros(System.getProperties(), _path);
	}
	
	final String path()
	{
		return _path;
	}
	
	final Collection<FilesystemProperties> properties()
	{
		return Collections.unmodifiableCollection(_properties);
	}
}