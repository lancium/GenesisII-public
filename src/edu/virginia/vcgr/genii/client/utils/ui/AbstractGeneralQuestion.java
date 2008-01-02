package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * @author mmm2a
 *
 * An partial implementation of the UIGeneralQuestion interface.  Please see the
 * interface in question for more details about it.
 */
public abstract class AbstractGeneralQuestion extends AbstractUIElement implements
		UIGeneralQuestion
{
	protected String _header;
	protected String _question;
	protected String _defaultValue;
	
	/**
	 * Construct a new general question abstract instance.
	 * 
	 * @param provider The provider that created this question.
	 * @param header The header (if any) to display.  May be null.
	 * @param question The question to display.  Cannot be null.
	 * @param defaultValue The default value (if any and applicable) of not
	 * values are entered.  This may be null.
	 */
	protected AbstractGeneralQuestion(UIProvider provider, 
		String header, String question, String defaultValue)
	{
		super(provider);
		
		if (question == null)
			throw new IllegalArgumentException("question parameter MUST be non-null.");
		
		_header = header;
		_question = question;
		_defaultValue = defaultValue;
	}
	
	@Override
	public String ask() throws UIException
	{
		while (true)
		{
			String value = internalAsk();
			if (value == null || value.length() == 0)
			{
				if (_defaultValue != null)
					return _defaultValue;
				else
					displayError("Please enter a value!");
			} else
				return value;
		}
	}
	
	/**
	 * Allow the child class to determine what is the best way to display the
	 * question and get the answer.
	 * 
	 * @return The string answer entered, or null or empty string if none were
	 * entered.
	 * 
	 * @throws UIException
	 */
	protected abstract String internalAsk() throws UIException;
}