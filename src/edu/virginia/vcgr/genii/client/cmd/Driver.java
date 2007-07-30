package edu.virginia.vcgr.genii.client.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import edu.virginia.vcgr.genii.client.ApplicationBase;

public class Driver extends ApplicationBase
{
	static public void usage() {
		System.out.println("Container [--config-dir=<config dir>]");
	}

	static public void main(String []args)
	{
		String explicitConfigDir = null;
		
		while (args.length > 0) {
			String arg = args[0];
			
			StringTokenizer st = new StringTokenizer(arg, "=");
			String option = st.nextToken();
			
			if (option.equals("--config-dir")) {
				if (!st.hasMoreElements()) {
					usage();
					return;
				}
				explicitConfigDir = st.nextToken();
				
				// strip off the arg 
				String[] newArgs = new String[args.length - 1];
				System.arraycopy(args, 1, newArgs, 0, newArgs.length);
				args = newArgs;
				
			} else {
				// dont recognize arg, assume it and all subsequent args 
				// are for the tool
				break;
			}
		}
		
		prepareClientApplication(explicitConfigDir);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		if (args.length == 0 || (
			args.length == 1 && args[0].equals("shell")))
			doShell(in);
		else
			doNonShell(in, args);
	}
	
	static private void doShell(BufferedReader in)
	{
		CommandLineRunner runner = new CommandLineRunner();
		IExceptionHandler exceptionHandler =
			ExceptionHandlerManager.getExceptionHandler();
		
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
					exceptionHandler.handleException(ioe, System.err);
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
			}
			catch (Throwable cause)
			{
				exceptionHandler.handleException(cause, System.err);
			}
		}
		
		System.exit(0);
	}
	
	static private void doNonShell(BufferedReader in, String []args)
	{
		CommandLineRunner runner = new CommandLineRunner();
		IExceptionHandler exceptionHandler =
			ExceptionHandlerManager.getExceptionHandler();
		
		try
		{	
			System.exit(
				runner.runCommand(args, System.out, System.err, in));
		}
		catch (Throwable cause)
		{
			exceptionHandler.handleException(cause, System.err);
			System.exit(1);
		}
	}
}