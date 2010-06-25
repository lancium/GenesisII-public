package edu.virginia.vcgr.genii.ui.utils;

import java.awt.Shape;

import javax.swing.Icon;

import edu.virginia.vcgr.genii.gjt.gui.GuiConstants;
import edu.virginia.vcgr.genii.gjt.gui.icons.Icons;
import edu.virginia.vcgr.genii.gjt.gui.icons.ShapeIcon;
import edu.virginia.vcgr.genii.gjt.gui.icons.Shapes;

public enum ShapeIcons
{
	Plus(Shapes.Plus.shape()),
	Minus(Shapes.Minus.shape()),
	UpArrow(Shapes.UpArrow.shape()),
	RightArrow(Shapes.RightArrow.shape()),
	DownArrow(Shapes.DownArrow.shape()),
	LeftArrow(Shapes.LeftArrow.shape());
	
	private Icon _normalIcon;
	private Icon _rolloverIcon;
	private Icon _disabledIcon;
	
	private ShapeIcons(Shape shape)
	{
		_normalIcon = new ShapeIcon(shape,
			GuiConstants.SIMPLE_ICON_BUTTON_COLOR, 0);
		_rolloverIcon = new ShapeIcon(shape,
			GuiConstants.SIMPLE_ICON_BUTTON_HIGHLIGHT_COLOR, 0);
		_disabledIcon = Icons.createGrayedIcon(_normalIcon);
			
	}
	
	public Icon normalIcon()
	{
		return _normalIcon;
	}
	
	public Icon rolloverIcon()
	{
		return _rolloverIcon;
	}
	
	public Icon disabledIcon()
	{
		return _disabledIcon;
	}
}