package edu.virginia.vcgr.genii.client.gui;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

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

	/**
	 * allows a new window to be dropped next to but not right on top of the previous one.
	 */
	static public void centerComponentWithOffset(Component comp, int xOffset, int yOffset)
	{
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();

		Point p = new Point(center.x - (comp.getWidth() / 2) + xOffset, center.y - (comp.getHeight() / 2) + yOffset);
		comp.setLocation(p);
	}
	//
	// static public void canterComponent(Component comp)
	// {
	// Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
	//
	// Point p = new Point(center.x - (comp.getWidth() / 2), center.y - (comp.getHeight() / 2));
	// comp.setLocation(p);
	// }
}