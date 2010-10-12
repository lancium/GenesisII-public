package edu.virginia.vcgr.genii.ui.utils.hover;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JDialog;

public interface HoverDialogProvider
{
	public boolean updatePosition(Component sourceComponent, Point position);
	public JDialog dialog();
}