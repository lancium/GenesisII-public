package edu.virginia.vcgr.genii.client.dialog;

public interface DialogProvider
{
	public InformationDialog createInformationDialog(String title, TextContent informationContent) throws DialogException,
		UserCancelException;

	public InformationDialog createErrorDialog(String title, TextContent errorContent) throws DialogException,
		UserCancelException;

	public InputDialog createInputDialog(String title, String prompt) throws DialogException, UserCancelException;

	public InputDialog createHiddenInputDialog(String title, String prompt) throws DialogException, UserCancelException;

	public ComboBoxDialog createComboBoxDialog(String title, String prompt, MenuItem defaultItem, MenuItem... items)
		throws DialogException, UserCancelException;

	public ComboBoxDialog createSingleListSelectionDialog(String title, String prompt, MenuItem defaultItem, MenuItem... items)
		throws DialogException, UserCancelException;

	public CheckBoxDialog createCheckBoxDialog(String title, String prompt, CheckBoxItem... items) throws DialogException,
		UserCancelException;

	public YesNoDialog createYesNoDialog(String title, String prompt, YesNoSelection defaultAnswer) throws DialogException,
		UserCancelException;
}