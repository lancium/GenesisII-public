package edu.virginia.vcgr.genii.client.utils.ui.text;

import edu.virginia.vcgr.genii.client.io.GetPassword;
import edu.virginia.vcgr.genii.client.utils.ui.AbstractUIElement;
import edu.virginia.vcgr.genii.client.utils.ui.UIException;
import edu.virginia.vcgr.genii.client.utils.ui.UIPassword;

/**
 * The text based implementation of the password widget.
 * 
 * @author mmm2a
 */
public class TextUIPassword extends AbstractUIElement implements UIPassword
{
	private String _header;
	private String _prompt;
	
	public TextUIPassword(TextUIProvider provider, String header, String prompt)
	{
		super(provider);
	
		if (prompt == null)
			throw new IllegalArgumentException("The password widget prompt cannot be null.");
		
		_header = header;
		_prompt = prompt;
	}
	
	@Override
	public char[] getPassword(String header, String question)
			throws UIException
	{
		TextUIProvider provider = (TextUIProvider)_provider;
		
		if (_header != null)
		{
			provider.getStdout().println(_header);
			provider.getStdout().println();
		}
		
		provider.getStdout().print(_prompt + "  ");
		provider.getStdout().flush();
		
		return GetPassword.getPassword("").toCharArray();
	}

	@Override
	public void displayError(String message) throws UIException
	{
		((TextUIProvider)_provider).displayError(message);
	}
}