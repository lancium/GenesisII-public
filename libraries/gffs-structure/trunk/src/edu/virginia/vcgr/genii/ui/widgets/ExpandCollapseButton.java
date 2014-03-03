package edu.virginia.vcgr.genii.ui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JToggleButton;

public class ExpandCollapseButton extends JToggleButton
{
	static final long serialVersionUID = 0L;

	static final private Dimension SIZE = new Dimension(6, 6);

	public ExpandCollapseButton(ActionListener... initialListeners)
	{
		for (ActionListener listener : initialListeners)
			addActionListener(listener);
		addActionListener(new InternalActionListener());
		setRolloverEnabled(true);
		setBorderPainted(false);

		setMinimumSize(SIZE);
		setPreferredSize(SIZE);
		setMaximumSize(SIZE);
	}

	@Override
	protected void paintComponent(Graphics _g)
	{
		Graphics2D g = (Graphics2D) _g.create();
		int w = getWidth();
		int h = getHeight();

		try {
			g.clearRect(0, 0, w, h);
			Color c = getForeground();
			if (getModel().isRollover())
				c = c.brighter();

			if (getModel().isSelected()) {
				g.fillPolygon(new int[] { 0, w - 1, (w - 1) / 2 }, new int[] { 0, 0, h - 1 }, 3);
			} else {
				g.fillPolygon(new int[] { 0, w - 1, 0 }, new int[] { 0, (h - 1) / 2, h - 1 }, 3);
			}
		} finally {
			g.dispose();
		}
	}

	private class InternalActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			repaint();
		}
	}
}