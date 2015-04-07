package edu.virginia.vcgr.genii.gjt.gui.util;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

public class GUIUtils
{
	static public void centerComponent(Component comp)
	{
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();

		Point p = new Point(center.x - (comp.getWidth() / 2), center.y - (comp.getHeight() / 2));
		comp.setLocation(p);
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

}