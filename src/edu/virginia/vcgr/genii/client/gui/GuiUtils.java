package edu.virginia.vcgr.genii.client.gui;

import java.awt.Component;
import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GraphicsUtils;

public class GuiUtils
{
	static private Log _logger = LogFactory.getLog(GuiUtils.class);

	static public boolean supportsGraphics()
	{
		return !GraphicsEnvironment.isHeadless();
	}

	static public void centerComponent(Component component)
	{
		GraphicsUtils.centerWindow(component);
	}

	static public void displayError(Component parentComponent, String title, Throwable cause)
	{
		_logger.warn("Exception occurred.", cause);

		String message = cause.getLocalizedMessage();
		if (message == null || message.trim().length() == 0)
			message = cause.getClass().getName();

		JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
	}
}