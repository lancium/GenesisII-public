package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * The widget interface for general question widgets.  General questions are
 * those without a pre-defined set of answers (except possibly a default
 * answer)
 * 
 * @author mmm2a
 */
public interface UIGeneralQuestion extends UIElement
{
	/**
	 * Ask the question of the user and get the response.
	 * @return The response that the user gave to the indicated question.
	 * 
	 * @throws UIException
	 */
	public String ask() throws UIException;
}