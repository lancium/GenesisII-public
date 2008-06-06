package edu.virginia.vcgr.genii.client.dialog.gui;

import javax.swing.JOptionPane;

import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.InformationDialog;
import edu.virginia.vcgr.genii.client.dialog.TextContent;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;

public class GuiErrorDialog implements InformationDialog
{
	private TextContent _help = null;
	
	private String _title;
	private TextContent _content;
	
	public GuiErrorDialog(String title, TextContent content)
	{
		_title = title;
		_content = content;
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
		JOptionPane.showMessageDialog(null, _content, _title, JOptionPane.ERROR_MESSAGE);
	}
}