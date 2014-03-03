package edu.virginia.vcgr.genii.client.dialog;

public interface InputDialog extends Dialog
{
	public void setDefaultAnswer(String defaultAnswer);

	public String getDefaultAnswer();

	public void setInputValidator(InputValidator validator);

	public InputValidator getInputValidator();

	public String getAnswer();
}