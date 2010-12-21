package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class CompositeIcon implements Icon
{
	private int _spacing;
	private Icon []_icons;
	
	CompositeIcon(int spacing, Icon...icons)
	{
		_spacing = spacing;
		
		if (icons.length <= 0)
			throw new IllegalArgumentException(
				"Must have at least one icon");
		
		_icons = icons;
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		boolean first = true;
		
		int height = getIconHeight();
		
		for (Icon icon : _icons)
		{
			if (!first)
				x += _spacing;
			first = false;
			icon.paintIcon(c, g, x,
				y + (height - icon.getIconHeight()) / 2);
			x += icon.getIconHeight();
		}
	}

	@Override
	public int getIconWidth()
	{
		int width = 0;
		
		for (Icon icon : _icons)
		{
			if (width > 0)
				width += _spacing;
			
			width += icon.getIconWidth();
		}
		
		return width;
	}

	@Override
	public int getIconHeight()
	{
		int height = 0;
		
		for (Icon icon : _icons)
			height = Math.max(height, icon.getIconHeight());
		
		return height;
	}
}