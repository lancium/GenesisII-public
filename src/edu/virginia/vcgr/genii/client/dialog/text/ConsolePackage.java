package edu.virginia.vcgr.genii.client.dialog.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.io.GetPassword;

class ConsolePackage
{
	private PrintStream _stdout;
	private PrintStream _stderr;
	private BufferedReader _stdin;
	
	public ConsolePackage(
		PrintStream stdout, PrintStream stderr, BufferedReader stdin)
	{
		_stdout = stdout;
		_stderr = stderr;
		_stdin = stdin;
	}
	
	public String readLine()
		throws DialogException
	{
		return readLine(false);
	}
	
	public String readLine(boolean hiddenInput)
		throws DialogException
	{
		try
		{
			if (hiddenInput)
			{
				return GetPassword.getPassword("");
			}
			
			return _stdin.readLine();
		}
		catch (IOException ioe)
		{
			throw new DialogException("Unable to read from standard in.", ioe);
		}
	}
	
	public PrintStream stdout()
	{
		return _stdout;
	}
	
	public PrintStream stderr()
	{
		return _stderr;
	}
}