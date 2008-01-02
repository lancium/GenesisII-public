package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * A widget interface for widgets that as OK/Cancel questions.
 * 
 * @author mmm2a
 */
public interface UIOKCancelQuestion extends UIElement
{
	/**
	 * Ask the user the stored ok/cancel question and get the answer.
	 * 
	 * @return an OK/Cancel response as selected by the user.
	 * 
	 * @throws UIException
	 */
	public UIOKCancelType ask() throws UIException;
}