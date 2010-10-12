package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.Icon;

import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;

public class BoxedIcon implements Icon
{	
	static final private int LINE_WIDTH = 2;
	static final private int SPACING = 2;
	static private Map<HistoryEventLevel, Color> BOX_COLORS;
	
	static
	{
		BOX_COLORS = new EnumMap<HistoryEventLevel, Color>(
			HistoryEventLevel.class);
		
		BOX_COLORS.put(HistoryEventLevel.Trace, Color.gray);
		BOX_COLORS.put(HistoryEventLevel.Debug, Color.blue);
		BOX_COLORS.put(HistoryEventLevel.Information, Color.white);
		BOX_COLORS.put(HistoryEventLevel.Warning, Color.yellow);
		BOX_COLORS.put(HistoryEventLevel.Error, Color.red);
	}

	private Color _boxColor;
	private Icon _baseIcon;
	
	public BoxedIcon(HistoryEventLevel level, Icon baseIcon)
	{
		_boxColor = BOX_COLORS.get(level);
		_baseIcon = baseIcon;
	}
	
	@Override
	final public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Graphics2D g2 = (Graphics2D)g.create();
		_baseIcon.paintIcon(c, g2, x + LINE_WIDTH + SPACING, y);

		g2.setColor(_boxColor);
		g2.fillRect(x, y, LINE_WIDTH, getIconHeight());
		
		g2.dispose();
	}

	@Override
	final public int getIconWidth()
	{
		return _baseIcon.getIconWidth() + LINE_WIDTH + SPACING;
	}

	@Override
	final public int getIconHeight()
	{
		return _baseIcon.getIconHeight();
	}
}