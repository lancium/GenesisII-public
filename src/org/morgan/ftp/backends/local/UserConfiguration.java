package org.morgan.ftp.backends.local;

import java.io.File;

public class UserConfiguration
{
	private String _user;
	private File _userDir;
	private String _password;
	private boolean _canRead;
	private boolean _canWrite;
	
	public UserConfiguration(String username, String password, File userDir,
		boolean canRead, boolean canWrite)
	{
		_user = username;
		_password = password;
		
		_userDir = userDir.getAbsoluteFile();
		
		_canRead = canRead;
		_canWrite = canWrite;
	}
	
	public String getUser()
	{
		return _user;
	}
	
	public File getUserDir()
	{
		return _userDir;
	}
	
	public boolean authenticate(String password)
	{
		if (password == null)
		{
			if (_password == null)
				return true;
			
			return false;
		}
		
		if (_password == null)
			return true;
		
		return _password.equals(password);
	}
	
	public boolean canRead()
	{
		return _canRead;
	}
	
	public boolean canWrite()
	{
		return _canWrite;
	}
}