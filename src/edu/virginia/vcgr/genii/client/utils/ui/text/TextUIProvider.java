package edu.virginia.vcgr.genii.client.utils.ui.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import edu.virginia.vcgr.genii.client.utils.ui.AbstractUIProvider;
import edu.virginia.vcgr.genii.client.utils.ui.UIException;
import edu.virginia.vcgr.genii.client.utils.ui.UIGeneralQuestion;
import edu.virginia.vcgr.genii.client.utils.ui.UIMenu;
import edu.virginia.vcgr.genii.client.utils.ui.UIProvider;

/**
 * This is the provider implementation for text based UIs.  This provider
 * provides "dialogs" which print output to stdout, error messages to stderr,
 * and reads inputs from stdin.
 * 
 * @author mmm2a
 */
public class TextUIProvider extends AbstractUIProvider implements UIProvider
{
	private PrintStream _stdout;
	private PrintStream _stderr;
	private BufferedReader _stdin;
	
	/**
	 * Create a new TextUIProvider with the given streams.
	 * 
	 * @param stdout The stream to use as stdout.
	 * @param stderr The stream to use as stderr.
	 * @param stdin The stream to use as stdin.
	 */
	public TextUIProvider(PrintStream stdout, PrintStream stderr, 
		BufferedReader stdin)
	{
		_stdout = stdout;
		_stderr = stderr;
		_stdin = stdin;
	}
	
	/**
	 * Retrieve the stdout stream for use by a widget.
	 * 
	 * @return The stdout stream.
	 */
	PrintStream getStdout()
	{
		return _stdout;
	}
	
	/**
	 * Retrieve the stderr stream for use by a widget.
	 * 
	 * @return The stderr stream.
	 */
	PrintStream getStderr()
	{
		return _stderr;
	}
	
	/**
	 * Retrieve the stdin stream for use by a widget.
	 * 
	 * @return The stdin stream.
	 */
	BufferedReader getStdin()
	{
		return _stdin;
	}
	
	/**
	 * A helper method for reading (and trimming) a line of input.
	 * 
	 * @return The trimmed line of input (or null)
	 * 
	 * @throws UIException
	 */
	String readTrimmedLine() throws UIException
	{
		try
		{
			String line = _stdin.readLine();
			if (line != null)
				return line.trim();
			
			return null;
		}
		catch (IOException ioe)
		{
			throw new UIException("Unable to read from standard in.", ioe);
		}
	}
	
	/**
	 * This is the actual implementation of the displayError method for the
	 * text-based UI widgets.  It prints to the stderr stream.
	 * 
	 * @param message The message to print.
	 */
	void displayError(String message)
	{
		_stderr.println(message);
	}
	
	@Override
	public UIGeneralQuestion createGeneralQuestion(String header,
			String question, String defaultValue) throws UIException
	{
		return new TextUIGeneralQuestion(this, header, question, defaultValue);
	}

	@Override
	public UIMenu createMenu(String header, String footer,
			boolean includeCancel, MenuElement[] elements) throws UIException
	{
		return new TextUIMenu(this, header, footer, includeCancel, elements);
	}
}