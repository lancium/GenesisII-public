package edu.virginia.vcgr.secrun.runnables.contpostboot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;

public class OwnerInfo
{
	private File _ownerInfoFile;
	
	private String _userPath;
	private String _userName;
	private String _userPassword;
	
	public OwnerInfo() throws IOException
	{
		_ownerInfoFile = Installation.getDeployment(
			new DeploymentName()).security().getSecurityFile("owner.info");
		
		FileReader reader = null;
		
		try
		{
			reader = new FileReader(_ownerInfoFile);
			BufferedReader bReader = new BufferedReader(reader);
			_userPath = bReader.readLine();
			_userName = bReader.readLine();
			_userPassword = bReader.readLine();
			
			if (_userPassword == null)
				throw new IOException("User info file is not correct.");
		}
		finally
		{
			StreamUtils.close(reader);
		}
	}
	
	public String getUserPath()
	{
		return _userPath;
	}
	
	public String getUserName()
	{
		return _userName;
	}
	
	public String getUserPassword()
	{
		return _userPassword;
	}
	
	public void deleteFile()
	{
		_ownerInfoFile.delete();
	}
	
	public boolean exists()
	{
		return _ownerInfoFile.exists();
	}
}