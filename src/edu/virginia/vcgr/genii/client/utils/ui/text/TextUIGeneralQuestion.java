package edu.virginia.vcgr.genii.client.utils.ui.text;

import edu.virginia.vcgr.genii.client.utils.ui.AbstractGeneralQuestion;
import edu.virginia.vcgr.genii.client.utils.ui.UIException;

/**
 * This is the text based implementation of the GeneralQuestion widget.  It
 * works by printing questions out to stdout, and reading answers in from
 * stdin.
 * 
 * @author mmm2a
 */
public class TextUIGeneralQuestion extends AbstractGeneralQuestion
{
	/**
	 * Create a new Text based General Question widget.
	 * 
	 * @param provider The provider that is creating this widget.
	 * @param header A header (if any) to display before the question.
	 * This value may be null.
	 * @param question The question to display (without a carriage return).
	 * This value cannot be null.
	 * @param defaultValue A default value to use if the user simply hits
	 * return.  This value may be null indicating that no defaults are allowed.
	 */
	public TextUIGeneralQuestion(
		TextUIProvider provider, String header, String question, 
		String defaultValue)
	{
		super(provider, header, question, defaultValue);
	}
	
	@Override
	protected String internalAsk() throws UIException
	{
		TextUIProvider provider = (TextUIProvider)_provider;
		
		if (_header != null)
		{
			provider.getStdout().println(_header);
			provider.getStdout().println();
		}
		
		provider.getStdout().print(_question + "  ");
		provider.getStdout().flush();
		
		return provider.readTrimmedLine();
	}

	@Override
	public void displayError(String message) throws UIException
	{
		((TextUIProvider)_provider).displayError(message);
	}
}