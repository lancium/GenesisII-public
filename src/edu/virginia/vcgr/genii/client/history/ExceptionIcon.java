package edu.virginia.vcgr.genii.client.history;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import edu.virginia.vcgr.genii.client.utils.icon.DefaultIconProvider;

public class ExceptionIcon implements Icon
{
	static final private Icon STACK_TRACE_ICON = DefaultIconProvider.createIconProvider(ExceptionIcon.class,
		"resources/stack-trace.png").createIcon();

	private Icon _baseIcon;

	public ExceptionIcon(Icon baseIcon)
	{
		_baseIcon = baseIcon;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Graphics2D g2 = (Graphics2D) g.create();

		_baseIcon.paintIcon(c, g2, x, y);
		STACK_TRACE_ICON.paintIcon(c, g2, x + (_baseIcon.getIconWidth() - STACK_TRACE_ICON.getIconWidth()), y);

		g2.dispose();
	}

	@Override
	public int getIconWidth()
	{
		return _baseIcon.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return _baseIcon.getIconHeight();
	}
}