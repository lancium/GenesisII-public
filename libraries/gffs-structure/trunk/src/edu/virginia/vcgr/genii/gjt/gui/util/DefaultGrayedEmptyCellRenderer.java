package edu.virginia.vcgr.genii.gjt.gui.util;

import java.awt.Color;

import javax.swing.table.DefaultTableCellRenderer;

public class DefaultGrayedEmptyCellRenderer extends DefaultTableCellRenderer
{
	static final long serialVersionUID = 0L;

	private Color _normalColor = null;
	private Color _emptyColor;
	private String _emptyLabel;

	public DefaultGrayedEmptyCellRenderer(String emptyLabel, Color emptyColor)
	{
		_emptyLabel = emptyLabel;
		_emptyColor = emptyColor;
	}

	public DefaultGrayedEmptyCellRenderer(String emptyLabel)
	{
		this(emptyLabel, Color.lightGray);
	}

	@Override
	protected void setValue(Object value)
	{
		if (_normalColor == null)
			_normalColor = getForeground();

		if (value != null) {
			if (!(value instanceof String) || ((String) value).length() > 0) {
				setForeground(_normalColor);
				super.setValue(value);
				return;
			}
		}

		setText(_emptyLabel);
		setForeground(_emptyColor);
	}
}