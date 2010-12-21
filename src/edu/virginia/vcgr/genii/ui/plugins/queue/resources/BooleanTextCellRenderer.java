package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class BooleanTextCellRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 0L;

	private String _trueText;
	private String _falseText;
	private Color _trueColor;
	private Color _falseColor;
	
	BooleanTextCellRenderer(String trueText, Color trueColor,
		String falseText, Color falseColor)
	{
		_trueText = trueText;
		_trueColor = trueColor;
		
		_falseText = falseText;
		_falseColor = falseColor;
	}
	
	BooleanTextCellRenderer(String trueText, String falseText)
	{
		this(trueText, Color.green.darker(), falseText, Color.red.darker());
	}
	
	BooleanTextCellRenderer()
	{
		this("True", Color.green.darker(), "False", Color.red.darker());
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		String text;
		
		Boolean bValue = (Boolean)value;
		
		if (bValue.booleanValue())
			text = _trueText;
		else
			text = _falseText;
		
		
		super.getTableCellRendererComponent(table, text, isSelected, hasFocus,
			row, column);
		
		if (bValue.booleanValue())
			setForeground(_trueColor);
		else
			setForeground(_falseColor);
		
		return this;
	}
}