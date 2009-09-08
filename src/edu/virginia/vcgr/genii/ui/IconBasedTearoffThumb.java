package edu.virginia.vcgr.genii.ui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;

import org.morgan.utils.gui.tearoff.TearoffThumb;

public class IconBasedTearoffThumb extends TearoffThumb
{
	static final long serialVersionUID = 0L;
	
	static private Icon _icon = Icons.tearoffIcon();
	
	static private Dimension getIconSize(Icon icon)
	{
		return new Dimension(icon.getIconWidth(), icon.getIconHeight());
	}
	
	public IconBasedTearoffThumb()
	{
		super(getIconSize(Icons.tearoffIcon()));
	}

	@Override
	protected void paintComponent(Graphics oldG)
	{
		_icon.paintIcon(this, oldG, 0, 0);
	}
}