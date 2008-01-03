package edu.virginia.vcgr.genii.client.utils.dialog.text;

import edu.virginia.vcgr.genii.client.utils.dialog.DialogException;
import edu.virginia.vcgr.genii.client.utils.dialog.GenericQuestionWidget;

/**
 * A text based general question widget.
 * 
 * @author mmm2a
 */
public class TextGeneralQuestionWidget extends AbstractTextWidget implements
		GenericQuestionWidget
{
	private String _answer = null;
	private String _default = null;
	private String _prompt = null;
	
	/**
	 * Construct a new text based general question widget.
	 * 
	 * @param provider The text provider for this widget.
	 * @param title The initial title.
	 */
	public TextGeneralQuestionWidget(TextWidgetProvider provider, String title)
	{
		super(provider, title);
	}
	
	@Override
	public String getAnswer()
	{
		return _answer;
	}

	@Override
	public void setDefault(String defaultAnswer)
	{
		_default = defaultAnswer;
	}

	@Override
	public void setPrompt(String prompt)
	{
		if (prompt == null)
			throw new IllegalArgumentException("Prompt cannot be null.");
		
		_prompt = prompt;
	}

	@Override
	public void showWidget() throws DialogException
	{
		if (_prompt == null)
			throw new IllegalArgumentException("Prompt has not be set yet.");
		
		TextWidgetProvider twp = TextWidgetProvider.class.cast(getProvider());
		
		_answer = null;
		
		String detailedHelp = getDetailedHelp();
		if (detailedHelp != null)
		{
			twp.stdout.println(detailedHelp);
			twp.stdout.println();
		}
		
		twp.stdout.flush();	
		
		String prompt = _prompt;
		if (_default != null)
			prompt = prompt + " [" + _default + "]";
		
		/* We make a second outcall to handle the prompt so that the password
		 * version of this widget can hide the characters that are typed in.
		 */
		String line = handlePrompt(twp, prompt);
		
		if (line.length() == 0 && _default != null)
			_answer = _default;
		else
			_answer = line;
	}
	
	/**
	 * Handle prompting the user and getting input.  THis method exists so that
	 * the password version of the dialog can hide the characters that are
	 * typed in.
	 * 
	 * @param twp The text provider who will be used to display the prompt
	 * @param prompt The prompt to display.
	 * 
	 * @return The answer typed in (without any logic applied to it's content).
	 * 
	 * @throws DialogException
	 */
	protected String handlePrompt(TextWidgetProvider twp, String prompt) 
		throws DialogException
	{
		twp.stdout.print(prompt + "  ");
		twp.stdout.flush();
		
		return twp.readline();
	}
}