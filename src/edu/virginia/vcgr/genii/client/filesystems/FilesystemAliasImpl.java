package edu.virginia.vcgr.genii.client.filesystems;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class FilesystemAliasImpl implements Filesystem
{
	private FilesystemManager _manager;
	private String _filesystemName;

	private Filesystem _trueFS = null;
	
	FilesystemAliasImpl(FilesystemManager manager, String filesystemName,
		FilesystemAliasConfiguration conf) throws FileNotFoundException
	{
		_manager = manager;
		_filesystemName = filesystemName;
		
		_trueFS = manager.lookup(conf.aliasFor());
		
		for (FilesystemSandboxConfiguration boxConf : conf.sandboxes())
			_trueFS.addSandbox(boxConf.name(), boxConf.relativePath(),
				boxConf.doCreate());
	}
	
	@Override
	public FilesystemWatchRegistration addWatch(Integer callLimit,
			long checkPeriod, TimeUnit checkPeriodUnits,
			FilesystemWatchFilter filter, FilesystemWatchHandler handler)
	{
		return _manager.addWatch(_filesystemName, this,
			callLimit, checkPeriod, checkPeriodUnits,
			filter, handler);
	}

	@Override
	public File filesystemRoot()
	{
		return _trueFS.filesystemRoot();
	}

	@Override
	public void addSandbox(String sandboxName, String relativePath,
		boolean doCreate) throws FileNotFoundException
	{
		_trueFS.addSandbox(sandboxName, relativePath, doCreate);
	}
	
	@Override
	public FilesystemSandbox openSandbox(String sandboxName)
			throws FileNotFoundException
	{
		return _trueFS.openSandbox(sandboxName);
	}

	@Override
	public Set<FilesystemProperties> properties()
	{
		return _trueFS.properties();
	}
}