package edu.virginia.vcgr.genii.client.cmd.tools.xscript.jsr;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Properties;

import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;

public class Grid
{
	private Properties _properties;
	private BufferedReader _stdin;
	private PrintWriter _stdout;
	private PrintWriter _stderr;

	public Grid(Properties properties,
		BufferedReader stdin, PrintWriter stdout, PrintWriter stderr)
	{
		_properties = properties;
		
		_stdin = stdin;
		_stdout = stdout;
		_stderr = stderr;
	}
	
	private int runCommandInternal(String command, String...arguments)
		throws Throwable
	{
		String []cLine = new String[arguments.length + 1];
		cLine[0] = command;
		for (int lcv = 0; lcv < arguments.length; lcv++)
			cLine[lcv + 1] = arguments[lcv];
			
		return
			new CommandLineRunner().runCommand(cLine, _stdout, _stderr, _stdin);
	}
	
	public Properties getProperties()
	{
		return _properties;
	}
	
	public int runCommand(String command)
		throws Throwable
	{
		return runCommandInternal(command);
	}
		
	public int runCommand(String command, String arg8)
		throws Throwable
	{
		return runCommandInternal(command, arg8);
	}
		
	public int runCommand(String command, String arg7,
		String arg8) throws Throwable
	{
		return runCommandInternal(command, arg7, arg8);
	}
		
	public int runCommand(String command, String arg6, String arg7,
		String arg8) throws Throwable
	{
		return runCommandInternal(command, arg6, arg7, arg8);
	}
		
	public int runCommand(String command, String arg5, String arg6,
		String arg7, String arg8) throws Throwable
	{
		return runCommandInternal(command, arg5, arg6, arg7, arg8);
	}
		
	public int runCommand(String command, String arg4, String arg5,
		String arg6, String arg7, String arg8) throws Throwable
	{
		return runCommandInternal(command, arg4, arg5,
			arg6, arg7, arg8);
	}
		
	public int runCommand(String command, String arg3, String arg4,
		String arg5, String arg6, String arg7, String arg8)
			throws Throwable
	{
		return runCommandInternal(command, arg3, arg4, arg5, arg6,
			arg7, arg8);
	}
		
	public int runCommand(String command, String arg2, String arg3,
		String arg4, String arg5, String arg6, String arg7, String arg8)
			throws Throwable
	{
		return runCommandInternal(command, arg2, arg3, arg4, arg5,
			arg6, arg7, arg8);
	}
		
	public int runCommand(String command, String arg1, String arg2,
		String arg3, String arg4, String arg5, String arg6, String arg7,
		String arg8) throws Throwable
	{
		return runCommandInternal(command, arg1, arg2, arg3, arg4, arg5,
			arg6, arg7, arg8);
	}
}
