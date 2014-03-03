package edu.virginia.vcgr.genii.client.dialog.gui;

import javax.swing.JOptionPane;

import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.TextContent;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.dialog.YesNoDialog;

public class GuiYesNoDialog implements YesNoDialog
{
	private boolean _isYes;

	private TextContent _help;

	private String _prompt;
	private String _title;

	public GuiYesNoDialog(String title, String prompt)
	{
		_title = title;
		_prompt = prompt;

		_help = null;
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

	@Override
	public TextContent getHelp()
	{
		return _help;
	}

	@Override
	public void setHelp(TextContent helpContent)
	{
		_help = helpContent;
	}

	@Override
	public void showDialog() throws DialogException, UserCancelException
	{
		int result = JOptionPane.showConfirmDialog(null, _prompt, _title, JOptionPane.YES_NO_CANCEL_OPTION);
		if (result == JOptionPane.CANCEL_OPTION)
			throw new UserCancelException();

		_isYes = (result == JOptionPane.YES_OPTION);
	}
}