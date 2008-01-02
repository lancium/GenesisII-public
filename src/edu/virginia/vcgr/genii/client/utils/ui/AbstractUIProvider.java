package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * This abstract class implements the common functionallity for a provider.
 *
 * @author mmm2a
 */
public abstract class AbstractUIProvider implements UIProvider
{
	/**
	 * Create a new YesNoQuestion widget.  This widget is independent of the
	 * provider because it delegates the display and input to a general
	 * question widget which IS provider specific.
	 * 
	 * @param header The header to print (if any) before the question is asked.
	 * This value may be null.
	 * @param question The question to ask the user.  This value cannot be null.
	 * @param defaultValue The default value to use if the user simply hits
	 * return.  This value can be null.
	 * @return The selected value.
	 * 
	 * @throws UIException
	 */
	public UIYesNoQuestion createYesNoQuestion(String header, String question,
		UIYesNoCancelType defaultValue) throws UIException
	{
		if (question == null)
			throw new IllegalArgumentException("question parameter cannot be null.");
		
		return new YesNoQuestion(this, header, question, defaultValue);
	}
	
	/**
	 * Create a new OKCancelQuestion widget.  This widget is independent of the
	 * provider because it delegates the display and input to a general
	 * question widget which IS provider specific.
	 * 
	 * @param header The header to print (if any) before the question is asked.
	 * This value may be null.
	 * @param question The question to ask the user.  This value cannot be null.
	 * @param defaultValue The default value to use if the user simply hits
	 * return.  This value can be null.
	 * @return The selected value.
	 * 
	 * @throws UIException
	 */
	public UIOKCancelQuestion createOKCancelQuestion(String header, 
		String question, UIOKCancelType defaultValue) throws UIException
	{
		if (question == null)
			throw new IllegalArgumentException("question parameter cannot be null.");
		
		return new OKCancelQuestion(this, header, question, defaultValue);
	}
}