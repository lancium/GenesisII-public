package edu.virginia.vcgr.genii.client.cmd.tools.gamllogin;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Collection;

import edu.virginia.vcgr.genii.client.gui.MenuDialog;
import edu.virginia.vcgr.genii.client.gui.PasswordDialog;
import edu.virginia.vcgr.genii.client.cmd.tools.GamlLoginTool.CertEntry;

public class GuiGamlLoginHandler extends AbstractGamlLoginHandler
{
	public GuiGamlLoginHandler(
		PrintStream out, PrintStream err, BufferedReader in)
	{
		super(out, err, in);
	}
	
	@Override
	protected char[] getPassword(String title, String prompt)
	{
		return PasswordDialog.getPassword(title, prompt);
	}

	@Override
	protected CertEntry selectCert(Collection<CertEntry> entries)
	{
		return MenuDialog.getMenuSelection("Certificate Selection", 
			"Please select desired certificate.", entries);
	}
}