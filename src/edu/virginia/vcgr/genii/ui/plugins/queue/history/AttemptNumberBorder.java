package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.border.AbstractBorder;

import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.client.history.HistoryEvent;

class AttemptNumberBorder extends AbstractBorder
{
	static final long serialVersionUID = 0L;
	static final int SPACING = 2;
	
	private Color _background;
	private Color _foreground;
	private Font _font;
	
	static private Integer attemptNumber(HistoryEvent event)
	{
		if (event != null)
		{
			String value = event.eventProperties().get("Attempt Number");
			if (value != null)
				return Integer.valueOf(value);
		}
		
		return null;
	}
	
	private Font getFont(Component c)
	{
		if (_font != null)
			return _font;
		
		return c.getFont().deriveFont(c.getFont().getSize() - 2.0f);
	}
	
	AttemptNumberBorder(Color background, Color foreground,
		Font font)
	{
		_background = background;
		_foreground = foreground;
		_font = font;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height)
	{
		Graphics2D g2 = (Graphics2D)g.create();
		
		JTree tree = (JTree)c;
		List<Pair<Integer, Rectangle>> boxes = 
			new ArrayList<Pair<Integer,Rectangle>>();
		Integer _currentAttempt = null;
		Point _currentStart = null;
		Insets insets = getBorderInsets(c);
		
		for (int row = 0; row < tree.getRowCount(); row++)
		{
			HistoryEventTreeNode node = (HistoryEventTreeNode)
				tree.getPathForRow(row).getLastPathComponent();
			Integer number = attemptNumber(node.event());
			
			if (number == null)
			{
				if (_currentAttempt != null)
				{
					Rectangle r = tree.getRowBounds(row);
					boxes.add(new Pair<Integer, Rectangle>(
						_currentAttempt, new Rectangle(
							_currentStart.x, _currentStart.y,
							insets.left, r.y - _currentStart.y - 1)));
					_currentAttempt = null;
					_currentStart = null;
				}
			} else
			{
				if (_currentAttempt != null)
				{
					if (_currentAttempt.equals(number))
						continue;
					
					Rectangle r = tree.getRowBounds(row);
					boxes.add(new Pair<Integer, Rectangle>(
						_currentAttempt, new Rectangle(
							_currentStart.x, _currentStart.y,
							insets.left, r.y - _currentStart.y - 1)));
					_currentAttempt = null;
					_currentStart = null;
				}
				
				if (_currentAttempt == null)
				{
					_currentAttempt = number;
					Rectangle r = tree.getRowBounds(row);
					_currentStart = new Point(0, r.y);		
				}
			}
		}
		
		if (_currentAttempt != null)
		{
			Rectangle r = tree.getRowBounds(tree.getRowCount() - 1);
			boxes.add(new Pair<Integer, Rectangle>(
				_currentAttempt, new Rectangle(
					_currentStart.x, _currentStart.y,
					insets.left, r.y - _currentStart.y - 1 + r.height)));
		}
		
		g2.setFont(getFont(c));
		for (Pair<Integer, Rectangle> pair : boxes)
		{
			Rectangle2D fr = g2.getFontMetrics().getStringBounds(
				pair.first().toString(), g2);
			
			g2.setColor(_background);
			Rectangle r = pair.second();
			g2.fillRoundRect(r.x, r.y, (int)(fr.getWidth() + (2 * SPACING)), r.height,
				3, 3);
			g2.setColor(_foreground);
			g2.drawRoundRect(r.x, r.y, (int)(fr.getWidth() + (2 * SPACING)), r.height,
				3, 3);
			
			g2.drawString(pair.first().toString(),
				r.x + SPACING,
				r.y + SPACING + (float)fr.getHeight());
		}
		
		g2.dispose();
	}

	@Override
	public Insets getBorderInsets(Component c)
	{
		Font font = getFont(c);
		Rectangle2D r = font.getStringBounds("10",
			new FontRenderContext(null, true, true));
		
		return new Insets(0, (int)(r.getWidth() + SPACING * 2),
			0, 0);
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets)
	{
		Insets i = getBorderInsets(c);
		
		insets.top = i.top;
		insets.left = i.left;
		insets.bottom = i.bottom;
		insets.right = i.right;
		
		return insets;
	}
}