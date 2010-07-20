package edu.virginia.vcgr.genii.client.filesystems;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = FilesystemConstants.CONFIGURATION_NS,
	name = "filesystems")
class FilesystemsConfiguration
{
	@XmlElement(namespace = FilesystemConstants.CONFIGURATION_NS,
		name = "filesystem")
	private Collection<FilesystemConfiguration> _filesystems =
		new LinkedList<FilesystemConfiguration>();
	
	@XmlElement(namespace = FilesystemConstants.CONFIGURATION_NS,
		name = "filesystem-alias")
	private Collection<FilesystemAliasConfiguration> _aliases =
		new LinkedList<FilesystemAliasConfiguration>();
	
	@XmlElement(namespace = FilesystemConstants.CONFIGURATION_NS,
		name = "filesystem-watcher", required = false)
	private Collection<FilesystemWatcherConfiguration> _watchers =
		new LinkedList<FilesystemWatcherConfiguration>();
	
	final Collection<FilesystemConfiguration> filesystems()
	{
		return Collections.unmodifiableCollection(_filesystems);
	}
	
	final Collection<FilesystemAliasConfiguration> aliases()
	{
		return Collections.unmodifiableCollection(_aliases);
	}
	
	final Collection<FilesystemWatcherConfiguration> watchers()
	{
		return Collections.unmodifiableCollection(_watchers);
	}
}