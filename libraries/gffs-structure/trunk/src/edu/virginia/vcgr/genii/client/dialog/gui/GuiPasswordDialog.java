package edu.virginia.vcgr.genii.client.dialog.gui;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class GuiPasswordDialog extends GuiInputDialog
{
	static final long serialVersionUID = 0L;

	public GuiPasswordDialog(String title, String prompt)
	{
		super(title, prompt);
	}

	@Override
	protected JTextField createTextField()
	{
		return new JPasswordField();
	}
}