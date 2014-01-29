package edu.virginia.vcgr.genii.client.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

public class GuiHelpAction extends AbstractAction
{
	static final long serialVersionUID = 0L;
	private String _url;
	private JDialog _d;

	public static void DisplayUrlHelp(String url)
	{
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.OPEN))
				try {
					desktop.browse(new URI(url));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		}

	}

	public GuiHelpAction(JDialog d, String helpUrl)
	{
		super("Help");
		_url = helpUrl;
		_d = d;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// _information = null;
		DisplayUrlHelp(_url);
	}
}
