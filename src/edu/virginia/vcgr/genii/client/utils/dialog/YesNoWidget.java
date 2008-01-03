package edu.virginia.vcgr.genii.client.utils.dialog;

/**
 * A simple widget to ask the user a yes/no question.  This widget
 * can be customized to also except cancel.
 *  
 * @author mmm2a
 */
public interface YesNoWidget extends Widget
{
	/**
	 * Tell the widget whether or not it is allowed to accept a cancel
	 * response as well.
	 * 
	 * @param includeCancel true if the widget will accept cancel, false
	 * otherwise.
	 */
	public void includeCancel(boolean includeCancel);
	
	/**
	 * Set the prompt to query the user with.
	 * 
	 * @param prompt The prompt to query the user with.
	 */
	public void setPrompt(String prompt);
	
	/**
	 * Set the default answer.
	 * 
	 * @param defaultAnswer A default answer to use when the user doesn't
	 * select anything.  This value can be null indicating that no defaults
	 * are accepted.  It is not permissible to set the default as Cancel when
	 * the dialog is not configured to accept cancel as a choice.
	 */
	public void setDefault(YesNoCancelType defaultAnswer);
	
	/**
	 * Get the answer that was selected.
	 * 
	 * @return The selected (entered) answer.
	 */
	public YesNoCancelType getAnswer();
}