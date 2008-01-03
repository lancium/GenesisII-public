package edu.virginia.vcgr.genii.client.utils.dialog.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import edu.virginia.vcgr.genii.client.utils.dialog.DialogException;
import edu.virginia.vcgr.genii.client.utils.dialog.GenericQuestionWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.MenuWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.OKCancelWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.PasswordWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.WidgetProvider;
import edu.virginia.vcgr.genii.client.utils.dialog.YesNoWidget;

/**
 * The Text based widget provider.
 * 
 * @author mmm2a
 */
public class TextWidgetProvider implements WidgetProvider
{
	public PrintStream stdout;
	public PrintStream stderr;
	public BufferedReader stdin;
	
	/**
	 * Construct a new text-based widget provider.
	 * 
	 * @param stdout The stdout stream.
	 * @param stderr The stderr stream.
	 * @param stdin The stdin stream.
	 */
	public TextWidgetProvider(PrintStream stdout, PrintStream stderr, 
		BufferedReader stdin)
	{
		this.stdout = stdout;
		this.stderr = stderr;
		this.stdin = stdin;
	}
	
	/**
	 * Read a line of input from the stdin stream.
	 * 
	 * @return The line read from standard in.
	 * 
	 * @throws DialogException
	 */
	public String readline() throws DialogException
	{
		try
		{
			String line = stdin.readLine();
			if (line == null)
				throw new DialogException(
					"Unexpected end of input from stdin.");
			
			return line.trim();
		}
		catch (IOException ioe)
		{
			throw new DialogException("Unable to read from stdin.", ioe);
		}
	}
	
	@Override
	public GenericQuestionWidget createGenericQuestionDialog(String title)
	{
		return new TextGeneralQuestionWidget(this, title);
	}

	@Override
	public MenuWidget createMenuDialog(String title)
	{
		return new TextMenuWidget(this, title);
	}

	@Override
	public OKCancelWidget createOKCancelDialog(String title)
	{
		return new TextOKCancelWidget(this, title);
	}

	@Override
	public PasswordWidget createPasswordDialog(String title)
	{
		return new TextPasswordWidget(this, title);
	}

	@Override
	public YesNoWidget createYesNoDialog(String title)
	{
		return new TextYesNoCancelWidget(this, title);
	}
}