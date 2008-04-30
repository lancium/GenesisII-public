package edu.virginia.vcgr.genii.client.nativeq;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * This exception is thrown when a fork/exec is attempted that
 * fails with a non-zero exit code.
 * 
 * @author mmm2a
 */
public class ScriptExecutionException extends NativeQueueException
{
	static final long serialVersionUID = 0L;
	
	private int _exitCode;
	
	/**
	 * Construct a new script execution exception.
	 * 
	 * @param commandLine The command line that failed.
	 * @param exitCode The resultant exit code of the failed command.
	 * @param exitMessage An exit message that was printed on stderr
	 * from the failed command.
	 */
	public ScriptExecutionException(Collection<String> commandLine,
		int exitCode, String exitMessage)
	{
		super(String.format(
			"Executing command resulted in non-zero exit code (%d).\n%s\n\n",
			exitCode, formErrorText(commandLine, exitMessage)));
		
		_exitCode = exitCode;
	}
	
	/**
	 * Retrieve the failed exit code.
	 * 
	 * @return The non-zero exit code that caused this failure.
	 */
	public int getExitCode()
	{
		return _exitCode;
	}
	
	static private Pattern WHITESPACE = Pattern.compile("^.*\\s+.*$");
	
	/**
	 * Form a useful error text from the command line and the exit
	 * message that the fork/exec'd application produced.
	 * 
	 * @param commandLine The command line that failed to execute successfully.
	 * @param stderr The resultant stderr text printed by the failed 
	 * application.
	 * @return A String that can be printed as part of the exception's
	 * display.
	 */
	static private String formErrorText(Collection<String> commandLine, 
		String stderr)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Command line:  ");
		boolean first = true;
		for (String c : commandLine)
		{
			if (!first)
				builder.append(" ");
			first = false;
			if (WHITESPACE.matcher(c).matches())
				builder.append("\"" + c + "\"");
			else
				builder.append(c);
		}
		builder.append("\n");
		
		
		builder.append("Stderr:\t");
		builder.append(stderr);
		
		return builder.toString();
	}
}