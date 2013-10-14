package edu.virginia.vcgr.genii.gjt.gui.variables;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import edu.virginia.vcgr.genii.gjt.data.Describer;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinition;
import edu.virginia.vcgr.genii.gjt.gui.util.DescribedLabel;

public class VariableDefinitionRenderer implements TableCellRenderer
{
	@SuppressWarnings("unchecked")
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
		int column)
	{
		VariableHistory history = (VariableHistory) value;

		DescribedLabel<VariableDefinition> label =
			new DescribedLabel<VariableDefinition>(history.current(),
				(Describer<VariableDefinition>) (history.current().describer()));

		if (hasFocus) {
			Border border = UIManager.getBorder("Table.focusCellHighlightBorder");
			if (border == null)
				border = BorderFactory.createLineBorder(Color.black);
			label.setBorder(border);
		}

		return label;
	}
}