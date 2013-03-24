package edu.virginia.vcgr.genii.client.filesystems;

import java.io.File;

final public class FilesystemUsageInformation
{
	private long _totalSpace;
	private long _totalAvailable;
	private long _totalUsable;
	private long _totalUsed;
	private double _percentUsed;
	private double _percentAvailable;

	public FilesystemUsageInformation(File file)
	{
		_totalSpace = file.getTotalSpace();
		_totalAvailable = file.getFreeSpace();
		_totalUsable = file.getUsableSpace();
		_totalUsed = _totalSpace - _totalAvailable;
		if (_totalSpace == 0) {
			_percentUsed = 100.0;
			_percentAvailable = 0.0;
		} else {
			_percentUsed = (double) _totalUsed / (double) _totalSpace * 100;
			_percentAvailable = (double) _totalAvailable / (double) _totalSpace * 100;
		}
	}

	final public long filesystemSize()
	{
		return _totalSpace;
	}

	final public long spaceAvailable()
	{
		return _totalAvailable;
	}

	final public long spaceUsable()
	{
		return _totalUsable;
	}

	final public long spaceUsed()
	{
		return _totalUsed;
	}

	final public double percentUsed()
	{
		return _percentUsed;
	}

	final public double percentAvailable()
	{
		return _percentAvailable;
	}

	@Override
	final public String toString()
	{
		return String.format(
			"FS Size = %d, FS Used = %d, FS Available = %d, FS Usable = %d, Percent Used = %.2f%%, Percent Avail = %.2f%%",
			_totalSpace, _totalUsed, _totalAvailable, _totalUsable, _percentUsed, _percentAvailable);
	}
}