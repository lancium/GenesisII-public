package edu.virginia.vcgr.genii.client.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.ApplicationBase;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;

public class Driver extends ApplicationBase
{
	static private Log _logger = LogFactory.getLog(Driver.class);
	
	static public void usage() 
	{
		System.out.println("Driver");
	}

	static public void main(String []args)
	{
		String deploymentName = System.getenv("GENII_DEPLOYMENT_NAME");
		if (deploymentName != null)
		{
			_logger.debug("Using Deployment \"" + deploymentName + "\".");
			System.setProperty(DeploymentName.DEPLOYMENT_NAME_PROPERTY,
				deploymentName);
		} else
		{
			_logger.debug("Using Deployment \"default\".");
		}
		
		prepareClientApplication();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		if (args.length == 0 || (
			args.length == 1 && args[0].equals("shell")))
			while(true) {
				try {
					doShell(in);
					break;
				} catch (ReloadShellException e) {}
			}
		else
		{
			try
			{
				doNonShell(in, args);
			}
			catch (ReloadShellException re) 
			{
			}
		}
	}
	
	static private void doShell(BufferedReader in) throws ReloadShellException
	{
		CommandLineRunner runner = new CommandLineRunner();
		
		while (true)
		{
			try
			{
				// get the current username (if available)
				String username = null;
	
				if (username != null) {
					System.out.print(username + "@vcgr:$>");
				} else {
					System.out.print("vcgr:$>");
				}
				System.out.flush();
				
				String line = null;
				try
				{
					line = in.readLine();
					if (line == null)
						break;
				}
				catch (IOException ioe)
				{
					ExceptionHandlerManager.getExceptionHandler().
						handleException(ioe, System.err);
					break;
				}
	
				String []args = CommandLineFormer.formCommandLine(line);
				if (args.length == 0)
					continue;
				
				int firstArg = 0;
				String toolName = args[0];
				
				// shell commands
				boolean displayElapsed = false;
				if ((toolName.compareToIgnoreCase("quit") == 0) || 
					(toolName.compareToIgnoreCase("exit") == 0))
					break;
				else if (toolName.compareToIgnoreCase("time") == 0)
				{
					displayElapsed = true;
					firstArg = 1;
					if (args.length > 1)
						toolName = args[1];
					else 
						continue;
				}
				
				long startTime = System.currentTimeMillis();
				String []passArgs = new String[args.length - firstArg];
				System.arraycopy(args, firstArg, passArgs, 0, passArgs.length);

				runner.runCommand(passArgs, System.out, System.err, in);
				
				long elapsed = System.currentTimeMillis() - startTime;
				
				if (displayElapsed)
				{
					long hours = elapsed / (60 * 60 * 1000);
					if (hours != 0) 
						elapsed = elapsed % (hours * 60 * 60 * 1000);
					long minutes = elapsed / (60 * 1000);
					if (minutes != 0) 
						elapsed = elapsed % (minutes * 60 * 1000);
					long seconds = elapsed / (1000);
					if (seconds != 0)
						elapsed = elapsed % (seconds * 1000);
					System.out.println("Elapsed time: " + hours + "h:" 
						+ minutes + "m:" + seconds + "s." + elapsed + "ms");
				}
			} catch (ReloadShellException e) {
				throw e;
			} catch (Throwable cause) {
				ExceptionHandlerManager.getExceptionHandler().
					handleException(cause, System.err);
			}
		}
		
		System.exit(0);
	}
	
	static private void doNonShell(BufferedReader in, String []args)
		throws ReloadShellException
	{
		CommandLineRunner runner = new CommandLineRunner();
		
		try
		{	
			System.exit(
				runner.runCommand(args, System.out, System.err, in));
		}
		catch (ReloadShellException re)
		{
			throw re;
		}
		catch (Throwable cause)
		{
			ExceptionHandlerManager.getExceptionHandler().
				handleException(cause, System.err);
			System.exit(1);
		}
	}
}
