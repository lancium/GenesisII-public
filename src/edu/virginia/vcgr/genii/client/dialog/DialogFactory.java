package edu.virginia.vcgr.genii.client.dialog;

import java.io.BufferedReader;
import java.io.PrintWriter;

import edu.virginia.vcgr.genii.client.configuration.UserPreferences;
import edu.virginia.vcgr.genii.client.dialog.gui.GuiDialogProvider;
import edu.virginia.vcgr.genii.client.dialog.text.TextDialogProvider;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;

public class DialogFactory
{
	static public DialogProvider getProvider(PrintWriter stdout, PrintWriter stderr, BufferedReader stdin,
		boolean allowGraphicsOverride)
	{
		if (!UserPreferences.preferences().preferGUI())
			allowGraphicsOverride = false;

		if (allowGraphicsOverride && GuiUtils.supportsGraphics())
			return new GuiDialogProvider();
		else
			return new TextDialogProvider(stdout, stderr, stdin);
	}
}