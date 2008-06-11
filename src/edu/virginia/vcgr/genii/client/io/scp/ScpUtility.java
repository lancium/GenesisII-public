package edu.virginia.vcgr.genii.client.io.scp;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;

public class ScpUtility
{
	static public void put(File localFile, 
		String user, String password,
		String host, int port, String remotePath)
		throws IOException
	{
		try
		{
			Scp copier = new Scp();
			copier.setPort(port);
			copier.setLocalFile(localFile.getAbsolutePath());
			copier.setRemoteTofile(String.format("%s@%s:%s",
				user, host, remotePath));
			copier.setPassword(password);
			copier.setSftp(false);
			copier.setTrust(true);
			copier.setProject(new Project());
			copier.execute();
		}
		catch (Throwable cause)
		{
			throw new IOException("Unable to use scp to put file.", cause);
		}
	}
	
	static public void get(File localFile,
		String user, String password,
		String host, int port, String remotePath)
		throws IOException
	{
		try
		{
			Scp copier = new Scp();
			copier.setPort(port);
			copier.setRemoteFile(String.format("%s@%s:%s",
				user, host, remotePath));
			copier.setLocalTofile(localFile.getAbsolutePath());
			copier.setPassword(password);
			copier.setSftp(false);
			copier.setTrust(true);
			copier.setProject(new Project());
			copier.execute();
		}
		catch (Throwable cause)
		{
			throw new IOException("Unable to use scp to get file.", cause);
		}
	}
}