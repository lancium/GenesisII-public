package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * A default implementation of the OKCancelQuestion widget.  This widget is
 * implemented outside of a provider because it simply delegates the work of
 * display and input to a general question widget (which is provider specific).
 * 
 * @author mmm2a
 */
public class OKCancelQuestion extends AbstractUIElement implements UIOKCancelQuestion
{
	private UIGeneralQuestion _question;
	
	/**
	 * Create a new OKCancel question widget.
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
	public OKCancelQuestion(UIProvider provider, 
		String header, String question,
		UIOKCancelType defaultValue) throws UIException
	{
		super(provider);
		
		if (question == null)
			throw new IllegalArgumentException(
				"Question parameter cannot be null.");
		
		/* Go ahead and create the general question widget that we'll use to
		 * ask the ok/cancel question.
		 */
		_question = provider.createGeneralQuestion(header, question,
			(defaultValue == null) ? null : defaultValue.name());
	}
	
	@Override
	public UIOKCancelType ask() throws UIException
	{
		while (true)
		{
			String value = _question.ask();
			if (value == null || value.length() == 0)
				displayError("Result must be one of OK or Cancel.");
			else
			{
				if (value.equalsIgnoreCase(UIOKCancelType.OK.name()))
					return UIOKCancelType.OK;
				else if (value.equalsIgnoreCase(UIOKCancelType.CANCEL.name()))
					return UIOKCancelType.CANCEL;
				
				displayError("Result must be one of OK or Cancel.");
			}
		}
	}

	@Override
	public void displayError(String message) throws UIException
	{
		_question.displayError(message);
	}
}
