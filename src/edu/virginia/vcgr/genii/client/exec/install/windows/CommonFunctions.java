package edu.virginia.vcgr.genii.client.exec.install.windows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.configuration.Installation;

public class CommonFunctions
{
	static private final String MACHINE_NAME_TOKEN = "$MACHINENAME";
	
	static public void checkPath(File path, boolean canExecute)
		throws IOException
	{
		if (!path.exists())
			throw new FileNotFoundException(String.format(
				"Unable to find file \"%s\".",
				path.getAbsolutePath()));
		
		if (!path.isFile())
			throw new IOException(String.format(
				"Path \"%s\" does not refer to a file.",
				path.getAbsolutePath()));
		
		if (canExecute)
		{
			if (!path.canExecute())
				throw new IOException(String.format(
					"Cannot execute file \"%s\".",
					path.getAbsolutePath()));
		} else
		{
			if (!path.canRead())
				throw new IOException(String.format(
					"Cannot read file \"%s\".",
					path.getAbsolutePath()));
		}
	}
	
	static public File getGeniiInstallDir()
	{
		return Installation.getInstallDirectory();
	}
	
	static public String getLocalUserDomain()
		throws IOException
	{
		String ret = System.getenv("USERDOMAIN");
		if (ret == null || ret.length() == 0)
			throw new IOException("Unable to acquire user domain.");
		
		return ret;
	}
	
	static public String getAccount(String accountOverride) throws IOException
	{
		if (accountOverride == null)
			accountOverride = String.format("%s\\%s",
				getLocalUserDomain(), System.getProperty("user.name"));
		else
		{
			if (accountOverride.contains(MACHINE_NAME_TOKEN))
			{
				accountOverride = accountOverride.replace(MACHINE_NAME_TOKEN,
					Hostname.getLocalHostname().toShortString());
			}
		}

		return accountOverride;
	}
}