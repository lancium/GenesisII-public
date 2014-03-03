package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;

import javax.swing.Icon;

public class IconFactory
{
	static private abstract class BaseIcon implements Icon
	{
		static final protected float STROKE_DIVISOR = 6.0f;
		static final private int SCALE_DIVISOR = 4;
		static final private int NEGATIVE_SCALE_DIVISOR = SCALE_DIVISOR - 1;

		protected int _short;
		protected int _long;

		protected int _size;

		private BaseIcon(int size)
		{
			_size = size;

			_short = size / SCALE_DIVISOR;
			_long = (NEGATIVE_SCALE_DIVISOR * size / SCALE_DIVISOR);
		}

		@Override
		final public int getIconWidth()
		{
			return _size;
		}

		@Override
		final public int getIconHeight()
		{
			return _size;
		}
	}

	static private class ErrorIcon extends BaseIcon
	{
		private ErrorIcon(int size)
		{
			super(size);
		}

		@Override
		final public void paintIcon(Component c, Graphics g, int x, int y)
		{
			Graphics2D g2 = (Graphics2D) g.create();
			Stroke stroke = new BasicStroke(Math.max(_size / STROKE_DIVISOR, 1.0f));
			g2.setStroke(stroke);
			g2.setColor(Color.red);

			g2.drawLine(x + _short, y + _short, x + _long, y + _long);
			g2.drawLine(x + _short, y + _long, x + _long, y + _short);

			g2.dispose();
		}
	}

	static private class WarningIcon extends BaseIcon
	{
		private Shape _shape;

		private WarningIcon(int size)
		{
			super(size);

			_shape = new Polygon(new int[] { size / 2, _long, _short }, new int[] { _short, _long, _long }, 3);
		}

		@Override
		final public void paintIcon(Component c, Graphics g, int x, int y)
		{
			Graphics2D g2 = (Graphics2D) g.create();

			if (x != 0 || y != 0)
				g2.translate(x, y);

			g2.setColor(Color.yellow);
			g2.fill(_shape);

			g2.setColor(Color.black);
			g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2.draw(_shape);

			g2.dispose();
		}
	}

	static private class EmptyIcon extends BaseIcon
	{
		private EmptyIcon(int size)
		{
			super(size);
		}

		@Override
		final public void paintIcon(Component c, Graphics g, int x, int y)
		{
		}
	}

	static public Icon createErrorIcon(int size)
	{
		return new ErrorIcon(size);
	}

	static public Icon createWarningIcon(int size)
	{
		return new WarningIcon(size);
	}

	static public Icon createEmptyIcon(int size)
	{
		return new EmptyIcon(size);
	}
}