package edu.virginia.vcgr.genii.gjt.gui.icons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

import javax.swing.Icon;

public class ShapeIcon implements Icon
{
	private Shape _shape;
	private int _border;
	private Color _color;

	private int getHeight()
	{
		return (int) (_shape.getBounds2D().getHeight() + 2 * _border);
	}

	private int getWidth()
	{
		return (int) (_shape.getBounds2D().getWidth() + 2 * _border);
	}

	public ShapeIcon(Shape shape, Color color, int border)
	{
		_shape = shape;
		_border = border;
		_color = color;
	}

	@Override
	public int getIconHeight()
	{
		return Math.max(getHeight(), getWidth());
	}

	@Override
	public int getIconWidth()
	{
		return Math.max(getHeight(), getWidth());
	}

	@Override
	public void paintIcon(Component c, Graphics _g, int x, int y)
	{
		Graphics2D g = null;

		try {
			g = (Graphics2D) _g.create();
			g.translate(x + _border, y + _border);

			g.setPaint(_color);
			g.fill(_shape);
		} finally {
			if (g != null)
				g.dispose();
		}
	}
}