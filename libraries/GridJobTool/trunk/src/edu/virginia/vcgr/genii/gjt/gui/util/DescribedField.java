package edu.virginia.vcgr.genii.gjt.gui.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JTextField;

import edu.virginia.vcgr.genii.gjt.data.Describer;

public class DescribedField<Type> extends JTextField {
	static final long serialVersionUID = 0L;

	static final private int BUFFER = 15;

	private Type _data;
	private Describer<Type> _describer;
	private DescribedFieldEditor<Type> _editor;

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

	public DescribedField(Type data, Describer<Type> describer,
			DescribedFieldEditor<Type> editor, int columns) {
		super(columns);
		setEditable(false);

		_data = data;
		_describer = describer;
		_editor = editor;

		addMouseListener(new MouseListenerImpl());
	}

	private class MouseListenerImpl extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (_editor != null) {
				_editor.edit(DescribedField.this, _data);
				repaint();
			}
		}
	}
}
