package edu.virginia.vcgr.genii.client.utils.dialog;

/**
 * A widget for prompting the user to enter a text answer to a question.
 * 
 * @author mmm2a
 */
public interface GenericQuestionWidget extends Widget
{
	/**
	 * Set the prompt which will be displayed to the user.
	 * 
	 * @param prompt The prompt to display to the user.
	 */
	public void setPrompt(String prompt);
	
	/**
	 * Sets a default answer that the widget will use if the user
	 * simply hit's enter.  This default will be displayed (somehow) with
	 * the prompt.  If the defaultAnswer is null, then no defaults are
	 * given and the dialog will simply return an empty string if the
	 * user doesn't enter his or her own answer.
	 * 
	 * @param defaultAnswer The default answer to use for this widget.
	 */
	public void setDefault(String defaultAnswer);
	
	/**
	 * Get the answer that the user typed in.
	 * 
	 * @return The answer that the user typed in.
	 */
	public String getAnswer();
}