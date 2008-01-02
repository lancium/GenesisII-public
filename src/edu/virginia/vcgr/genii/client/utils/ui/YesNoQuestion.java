package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * A default implementation of the YesNoCancelQuestion widget.  This widget is
 * implemented outside of a provider because it simply delegates the work of
 * display and input to a general question widget (which is provider specific).
 * 
 * @author mmm2a
 */
public class YesNoQuestion extends AbstractUIElement implements UIYesNoQuestion
{
	private UIGeneralQuestion _question;
	
	/**
	 * Create a new YesNoCancel question widget.
	 * 
	 * @param provider The provider responsible for this widget.
	 * @param header The header (if any) to use before the question is
	 * displayed.  This value can be null.
	 * @param question The question to ask the user.  THis value cannot
	 * be null.
	 * @param defaultValue A default value to use if the user simply
	 * hits return.  This value can be null.
	 * 
	 * @throws UIException
	 */
	public YesNoQuestion(UIProvider provider, 
		String header, String question,
		UIYesNoCancelType defaultValue) throws UIException
	{
		super(provider);
		
		/* Go ahead and create the general question widget that we'll use to
		 * ask the yes/no/cancel question.
		 */
		_question = provider.createGeneralQuestion(header, question,
			(defaultValue == null) ? null : defaultValue.name());
	}
	
	@Override
	public UIYesNoCancelType ask() throws UIException
	{
		while (true)
		{
			String value = _question.ask();
			if (value == null || value.length() == 0)
				displayError("Result must be one of Yes, No, or Cancel.");
			else
			{
				if (value.equalsIgnoreCase(UIYesNoCancelType.YES.name()))
					return UIYesNoCancelType.YES;
				else if (value.equalsIgnoreCase(UIYesNoCancelType.NO.name()))
					return UIYesNoCancelType.NO;
				else if (value.equalsIgnoreCase(UIYesNoCancelType.CANCEL.name()))
					return UIYesNoCancelType.CANCEL;
				
				displayError("Result must be one of Yes, No, or Cancel.");
			}
		}
	}

	@Override
	public void displayError(String message) throws UIException
	{
		_question.displayError(message);
	}
}
