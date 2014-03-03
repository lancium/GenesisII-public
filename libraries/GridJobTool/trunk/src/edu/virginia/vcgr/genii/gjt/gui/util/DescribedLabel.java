package edu.virginia.vcgr.genii.gjt.gui.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;

import edu.virginia.vcgr.genii.gjt.data.Describer;

public class DescribedLabel<Type> extends JLabel {
	static final long serialVersionUID = 0L;

	static final private int BUFFER = 15;

	private Type _data;
	private Describer<Type> _describer;

	@Override
	protected void paintComponent(Graphics _g) {
		String toDisplay = "...";
		Graphics2D g = (Graphics2D) _g;

		for (int verbosity = _describer.maximumVerbosity(); verbosity >= 0; verbosity--) {
			toDisplay = _describer.describe(_data, verbosity);
			if (verbosity == 0)
				break;

			Rectangle2D rectangle = g.getFont().getStringBounds(toDisplay,
					g.getFontRenderContext());
			if (rectangle.getWidth() <= (getWidth() - BUFFER))
				break;
		}

		setText(toDisplay);
		super.paintComponent(g);
	}

	public DescribedLabel(Type data, Describer<Type> describer) {
		_data = data;
		_describer = describer;
	}
}
