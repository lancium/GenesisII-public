package edu.virginia.vcgr.genii.client.filesystems;

import javax.xml.bind.annotation.XmlAttribute;

class FilesystemAliasConfiguration extends FilesystemSandboxContainerConfiguration
{
	@XmlAttribute(name = "alias-for", required = true)
	private String _aliasFor = null;

	final String aliasFor()
	{
		return _aliasFor;
	}
}