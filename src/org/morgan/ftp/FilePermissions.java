package org.morgan.ftp;

public class FilePermissions
{
	static public final int EXEC_PERM = 0x1;
	static public final int WRITE_PERM = 0x2;
	static public final int READ_PERM = 0x4;
	
	private int _userPerms;
	private int _groupPerms;
	private int _worldPerms;
	
	public FilePermissions(int user, int group, int world)
	{
		_userPerms = user;
		_groupPerms = group;
		_worldPerms = world;
	}
	
	static private String toString(int perm)
	{
		return String.format("%1$s%2$s%3$s", 
			((perm & READ_PERM) > 0) ? "r" : "-",
			((perm & WRITE_PERM) > 0) ? "w" : "-",
			((perm & EXEC_PERM) > 0) ? "x" : "-");
			
	}
	
	public String toString()
	{
		return toString(_userPerms) + toString(_groupPerms) + toString(_worldPerms);
	}
}