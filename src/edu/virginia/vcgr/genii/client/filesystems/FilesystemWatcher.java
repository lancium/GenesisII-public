package edu.virginia.vcgr.genii.client.filesystems;

import java.util.Calendar;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class FilesystemWatcher
{
	static private Log _logger = LogFactory.getLog(FilesystemWatcher.class);

	private long _checkPeriod;
	private Calendar _nextCheck;
	private String _filesystemName;
	private Filesystem _filesystem;
	private FilesystemWatchFilter _filter;
	private Collection<FilesystemWatchCallback> _callbacks;
	volatile private boolean _cancelled = false;

	private Calendar setNextCheck()
	{
		_nextCheck.setTimeInMillis(_nextCheck.getTimeInMillis() + _checkPeriod);
		return _nextCheck;
	}

	private void performCallbacks(FilesystemManager manager, FilesystemUsageInformation usageInformation, boolean matched)
	{
		for (FilesystemWatchCallback callback : _callbacks) {
			if (!_cancelled)
				callback.performCallback(manager, _filesystemName, _filesystem, usageInformation, matched);
			else
				return;
		}
	}

	FilesystemWatcher(long checkPeriod, String filesystemName, Filesystem filesystem, FilesystemWatchFilter filter,
		Collection<FilesystemWatchCallback> callbacks)
	{
		_checkPeriod = checkPeriod;
		if (_logger.isTraceEnabled())
			_logger.trace(String.format("using fs check interval of %d seconds.", _checkPeriod / 1000));
		_filesystemName = filesystemName;
		_filesystem = filesystem;
		_filter = filter;
		_callbacks = callbacks;

		_nextCheck = Calendar.getInstance();
	}

	final Calendar nextCheck()
	{
		return _nextCheck;
	}

	final Calendar performCheck(FilesystemManager manager)
	{
		FilesystemUsageInformation usageInformation = _filesystem.currentUsage();

		if (_logger.isTraceEnabled())
			_logger.trace(String.format("Performing check on %s:\n%s\n", _filesystem.filesystemRoot(), usageInformation));

		performCallbacks(manager, usageInformation, _filter.matches(usageInformation));

		return setNextCheck();
	}

	final void cancel()
	{
		_cancelled = true;
	}

	final boolean cancelled()
	{
		return _cancelled;
	}
}
