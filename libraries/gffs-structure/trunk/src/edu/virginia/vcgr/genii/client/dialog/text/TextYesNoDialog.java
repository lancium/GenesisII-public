package edu.virginia.vcgr.genii.client.dialog.text;

import edu.virginia.vcgr.genii.client.dialog.InputValidator;
import edu.virginia.vcgr.genii.client.dialog.YesNoDialog;
import edu.virginia.vcgr.genii.client.dialog.YesNoSelection;

public class TextYesNoDialog extends TextInputDialog implements YesNoDialog
{
	private boolean _isYes;

	public TextYesNoDialog(String title, ConsolePackage pkg, String prompt, YesNoSelection defaultAnswer)
	{
		super(title, pkg, prompt);

		if (defaultAnswer != null)
			setDefaultAnswer(defaultAnswer.name());

		setInputValidator(new InternalInputValidator());
	}

	@Override
	public boolean isNo()
	{
		return !_isYes;
	}

	@Override
	public boolean isYes()
	{
		return _isYes;
	}

	private class InternalInputValidator implements InputValidator
	{
		@Override
		public String validateInput(String input)
		{
			if (input.equalsIgnoreCase(YesNoSelection.YES.name())) {
				_isYes = true;
				return null;
			} else if (input.equalsIgnoreCase(YesNoSelection.NO.name())) {
				_isYes = false;
				return null;
			}

			return String.format("Please select either %s, or %s!", YesNoSelection.YES.name(), YesNoSelection.NO.name());
		}
	}
}