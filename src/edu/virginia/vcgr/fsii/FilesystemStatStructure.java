package edu.virginia.vcgr.fsii;

import edu.virginia.vcgr.fsii.security.Permissions;

public class FilesystemStatStructure
{
	private String _name;
	private FilesystemEntryType _entryType;
	
	private long _size;
	
	private long _created;
	private long _lastModified;
	private long _lastAccessed;
	
	private int _inode;
	
	private Permissions _permissions;
	
	public FilesystemStatStructure(int inode, String name, 
		FilesystemEntryType entryType, long size, long created, 
		long lastModified, long lastAcceessed, Permissions permissions)
	{
		_inode = inode;
		_name = name;
		_entryType = entryType;
		_size = size;
		_created = created;
		_lastModified = lastModified;
		_lastAccessed = lastAcceessed;
		_permissions = permissions;
	}
	
	public int getINode()
	{
		return _inode;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public FilesystemEntryType getEntryType()
	{
		return _entryType;
	}
	
	public long getSize()
	{
		return _size;
	}
	
	public long getCreated()
	{
		return _created;
	}
	
	public long getLastModified()
	{
		return _lastModified;
	}
	
	public long getLastAccessed()
	{
		return _lastAccessed;
	}
	
	public Permissions getPermissions()
	{
		return _permissions;
	}
}