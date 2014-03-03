package edu.virginia.vcgr.genii.client.dialog.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.io.GetPassword2;

class ConsolePackage
{
	private PrintWriter _stdout;
	private PrintWriter _stderr;
	private BufferedReader _stdin;

	public ConsolePackage(PrintWriter stdout, PrintWriter stderr, BufferedReader stdin)
	{
		_stdout = stdout;
		_stderr = stderr;
		_stdin = stdin;
	}

	public String readLine() throws DialogException
	{
		return readLine(false);
	}

	public String readLine(boolean hiddenInput) throws DialogException
	{
		try {
			if (hiddenInput) {
				return GetPassword2.getPassword("");
			}

			return _stdin.readLine();
		} catch (IOException ioe) {
			throw new DialogException("Unable to read from standard in.", ioe);
		}
	}

	public PrintWriter stdout()
	{
		return _stdout;
	}

	public PrintWriter stderr()
	{
		return _stderr;
	}
}