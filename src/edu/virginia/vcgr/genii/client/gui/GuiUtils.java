package edu.virginia.vcgr.genii.client.gui;

import java.awt.Component;
import java.awt.GraphicsEnvironment;

import org.morgan.util.GraphicsUtils;

public class GuiUtils
{
	static public boolean supportsGraphics()
	{
		return !GraphicsEnvironment.isHeadless();
	}
	
	static public void centerComponent(Component component)
	{
		GraphicsUtils.centerWindow(component);
	}
}