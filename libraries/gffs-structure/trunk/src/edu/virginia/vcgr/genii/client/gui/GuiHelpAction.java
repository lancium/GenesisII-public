package edu.virginia.vcgr.genii.client.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GuiHelpAction extends AbstractAction
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(GuiHelpAction.class);
	
	private String _url;
	
	public static void DisplayUrlHelp(String url)
	{
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.OPEN))
				try {
					desktop.browse(new URI(url));
				} catch (IOException e1) {
					_logger.error("caught unexpected exception", e1);
				} catch (URISyntaxException e1) {
					_logger.error("caught unexpected exception", e1);
				}
		}

	}

	public GuiHelpAction(JDialog d, String helpUrl)
	{
		super("Help");
		_url = helpUrl;
		// _d = d;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// _information = null;
		DisplayUrlHelp(_url);
	}
}
