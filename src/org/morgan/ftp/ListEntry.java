package org.morgan.ftp;

import java.util.Date;

public class ListEntry
{
	static private final long _180Days =
		1000 * 60 * 60 * 24 * 180;
	
	private boolean _isDirectory;
	private int _hardLinkCount;
	private FilePermissions _permissions;
	private String _user;
	private String _group;
	private long _size;
	private Date _lastMod;
	private String _name;
	
	public ListEntry(String name, Date lastMod, long size,
		String user, String group, FilePermissions permissions,
		int hardLinkCount, boolean isDirectory)
	{
		_isDirectory = isDirectory;
		_hardLinkCount = hardLinkCount;
		_permissions = permissions;
		_user = user;
		_group = group;
		_size = size;
		_lastMod = lastMod;
		_name = name;
	}
	
	public String toString()
	{
		String dateString;
		Date now = new Date();
		
		if (now.getTime() - _lastMod.getTime() > _180Days)
		{
			// long ago
			dateString = String.format("%1$tb %1$te %1$tY", _lastMod);
		} else
		{
			// recent
			dateString = String.format("%1$tb %1$te %1$tR", _lastMod);
		}
		
		return String.format("%1$s%2$s %3$d %4$s %5$s %6$d %7$s %8$s",
			(_isDirectory ? "d" : "-"),
			_permissions, _hardLinkCount, _user, _group, _size, dateString, _name);
	}
	
	public String getName()
	{
		return _name;
	}
}