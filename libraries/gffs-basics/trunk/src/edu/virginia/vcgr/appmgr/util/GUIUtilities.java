package edu.virginia.vcgr.appmgr.util;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

public class GUIUtilities
{
	static public void centerWindow(Component comp)
	{
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();

		Point p = new Point(center.x - (comp.getWidth() / 2), center.y - (comp.getHeight() / 2));
		comp.setLocation(p);
	}
}
