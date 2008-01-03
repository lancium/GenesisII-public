package edu.virginia.vcgr.genii.client.utils.dialog.text;

import edu.virginia.vcgr.genii.client.io.GetPassword;
import edu.virginia.vcgr.genii.client.utils.dialog.DialogException;
import edu.virginia.vcgr.genii.client.utils.dialog.PasswordWidget;

/**
 * A text-based password widget.
 * 
 * @author mmm2a
 */
public class TextPasswordWidget extends TextGeneralQuestionWidget implements
		PasswordWidget
{
	/**
	 * Construct a new text based password widget.
	 * 
	 * @param provider The text provider for this widget.
	 * @param title The initial title.
	 */
	public TextPasswordWidget(TextWidgetProvider provider, String title)
	{
		super(provider, title);
	}
	
	@Override
	public void setDefault(String defaultAnswer)
	{
		if (defaultAnswer != null)
			throw new IllegalArgumentException(
				"Not allowed to set default answers for password widgets.");
	}
	
	@Override
	protected String handlePrompt(TextWidgetProvider twp, String prompt) 
		throws DialogException
	{
		return GetPassword.getPassword(prompt);
	}
}