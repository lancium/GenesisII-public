package edu.virginia.vcgr.genii.client.dialog.gui;

import edu.virginia.vcgr.genii.client.dialog.CheckBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.CheckBoxItem;
import edu.virginia.vcgr.genii.client.dialog.ComboBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.DialogProvider;
import edu.virginia.vcgr.genii.client.dialog.InformationDialog;
import edu.virginia.vcgr.genii.client.dialog.InputDialog;
import edu.virginia.vcgr.genii.client.dialog.MenuItem;
import edu.virginia.vcgr.genii.client.dialog.TextContent;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.dialog.YesNoDialog;
import edu.virginia.vcgr.genii.client.dialog.YesNoSelection;

public class GuiDialogProvider implements DialogProvider
{
	@Override
	public InformationDialog createErrorDialog(String title,
			TextContent errorContent) throws DialogException,
			UserCancelException
	{
		return new GuiErrorDialog(title, errorContent);
	}

	@Override
	public InputDialog createHiddenInputDialog(String title, String prompt)
			throws DialogException, UserCancelException
	{
		return new GuiPasswordDialog(title, prompt);
	}

	@Override
	public InformationDialog createInformationDialog(String title,
			TextContent informationContent) throws DialogException,
			UserCancelException
	{
		return new GuiInformationDialog(title, informationContent);
	}

	@Override
	public InputDialog createInputDialog(String title, String prompt)
			throws DialogException, UserCancelException
	{
		return new GuiInputDialog(title, prompt);
	}

	@Override
	public ComboBoxDialog createComboBoxDialog(String title, String prompt,
			MenuItem defaultItem, MenuItem... items) throws DialogException,
			UserCancelException
	{
		return new GuiComboBoxDialog(title, prompt, defaultItem, items);
	}
	
	@Override
	public ComboBoxDialog createSingleListSelectionDialog(
		String title, String prompt, MenuItem defaultItem,
		MenuItem...items) throws DialogException, UserCancelException
	{
		return new GuiSingleSelectionListDialog(title, prompt, defaultItem, items);
	}

	@Override
	public CheckBoxDialog createCheckBoxDialog(String title, String prompt,
			CheckBoxItem...items) throws DialogException, UserCancelException
	{
		return new GuiCheckBoxDialog(title, prompt, items);
	}

	@Override
	public YesNoDialog createYesNoDialog(String title, String prompt,
			YesNoSelection defaultAnswer) throws DialogException,
			UserCancelException
	{
		return new GuiYesNoDialog(title, prompt);
	}
}
