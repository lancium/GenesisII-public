package edu.virginia.vcgr.genii.configurer.conf;

import java.io.File;

public class PostInstallationConfigurer
{
	static public void main(String []args)
	{
		try
		{
			if (args.length == 2)
			{
				if (args[0].equalsIgnoreCase("connect-client"))
				{
					connectClient(new File(args[1]));
					return;
				} else if (args[0].equalsIgnoreCase("start-container"))
				{
					startContainer(new File(args[1]));
					return;
				}
			}
			
			System.err.println(
				"USAGE:  PostInstallationConfigurer " +
					"<connect-client|start-container> <properties>");
			System.exit(1);
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
			System.exit(1);
		}
	}
	
	static private void connectClient(File propertiesFile)
	{
		
	}
	
	static private void startContainer(File propertiesFile)
	{
		
	}
}