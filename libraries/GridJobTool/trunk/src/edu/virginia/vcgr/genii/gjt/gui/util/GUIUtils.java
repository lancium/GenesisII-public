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
}