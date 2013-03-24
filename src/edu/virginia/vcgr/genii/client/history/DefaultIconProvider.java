package edu.virginia.vcgr.genii.client.history;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import edu.virginia.vcgr.genii.client.utils.icon.IconProvider;

class DefaultIconProvider implements IconProvider
{
	private int _iconSize;

	DefaultIconProvider(int iconSize)
	{
		_iconSize = iconSize;
	}

	@Override
	public Icon createIcon()
	{
		return new Icon()
		{
			@Override
			final public void paintIcon(Component c, Graphics g, int x, int y)
			{
				final Color ICON_COLOR = Color.blue;

				Graphics2D g2 = (Graphics2D) (g.create());
				g2.setColor(ICON_COLOR);

				g2.fillOval(x + 2, y + 2, _iconSize - 4, _iconSize - 4);
				g2.dispose();
			}

			@Override
			final public int getIconWidth()
			{
				return _iconSize;
			}

			@Override
			final public int getIconHeight()
			{
				return _iconSize;
			}
		};
	}
}