package edu.virginia.vcgr.genii.client.cmd.tools.login;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Collection;

import edu.virginia.vcgr.genii.client.gui.MenuDialog;
import edu.virginia.vcgr.genii.client.gui.PasswordDialog;
import edu.virginia.vcgr.genii.client.cmd.tools.login.CertEntry;

public class GuiLoginHandler extends AbstractLoginHandler
{
	public GuiLoginHandler(PrintWriter out, PrintWriter err, BufferedReader in)
	{
		super(out, err, in);
	}

	@Override
	public char[] getPassword(String title, String prompt)
	{
		return PasswordDialog.getPassword(title, prompt);
	}

	@Override
	protected CertEntry selectCert(Collection<CertEntry> entries)
	{
		return MenuDialog.getMenuSelection("Certificate Selection", "Please select desired certificate.", entries);
	}
}