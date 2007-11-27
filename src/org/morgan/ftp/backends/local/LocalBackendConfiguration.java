package org.morgan.ftp.backends.local;

import java.io.File;
import java.util.HashMap;

public class LocalBackendConfiguration
{
	private HashMap<String, UserConfiguration> _users = new HashMap<String, UserConfiguration>();
	
	public void addUser(String username, String password, File userDir, boolean canRead, boolean canWrite)
	{
		_users.put(username, new UserConfiguration(username, password, userDir, canRead, canWrite));
	}
	
	public UserConfiguration findUser(String userName)
	{
		return _users.get(userName);
	}
}