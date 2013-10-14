package edu.virginia.vcgr.genii.client.dialog;

public interface InputValidator
{
	/**
	 * This function is used by the dialog widget set to validate input for standard text questions.
	 * 
	 * @param input
	 *            The input to validate.
	 * @return A string message describing why the input is invalid, or null if the input is valid.
	 */
	public String validateInput(String input);
}