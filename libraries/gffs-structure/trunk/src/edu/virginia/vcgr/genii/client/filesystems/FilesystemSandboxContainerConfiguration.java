package edu.virginia.vcgr.genii.client.filesystems;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

class FilesystemSandboxContainerConfiguration
{
	@XmlAttribute(name = "name", required = true)
	private String _name = null;

	@XmlElement(namespace = FilesystemConstants.CONFIGURATION_NS, name = "filesystem-sandbox", required = false)
	private Collection<FilesystemSandboxConfiguration> _sandboxes = new LinkedList<FilesystemSandboxConfiguration>();

	final String name()
	{
		return _name;
	}

	final Collection<FilesystemSandboxConfiguration> sandboxes()
	{
		return Collections.unmodifiableCollection(_sandboxes);
	}
}