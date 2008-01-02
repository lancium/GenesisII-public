package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * This interface represents widgets which ask the user to answer a question
 * or prompt with a yes, no, or cancel answer.
 * 
 * @author mmm2a
 */
public interface UIYesNoQuestion extends UIElement
{
	/**
	 * Ask the user the stored question and get the answer back.
	 * 
	 * @return The selected yes/no/cancel option.
	 * 
	 * @throws UIException
	 */
	public UIYesNoCancelType ask() throws UIException;
}